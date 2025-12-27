package ai.data.pipeline.spring;

import ai.data.pipeline.spring.domain.Customer;
import ai.data.pipeline.spring.mapper.CustomerFieldMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {

    @Value("${spring.batch.chunk-size:10}")
    private int chunkSize;

    private static final String saveSql = """
        insert into customer.customers(email,first_name,last_name,phone,address,city,state,zip)\s
        values (:contact.email,
                :firstName,
                :lastName,
                :contact.phone,\s
                :location.address,
                :location.city,
                :location.state,
                :location.zip)\s
        on CONFLICT (email)\s
        DO UPDATE SET first_name = :firstName,\s
                last_name = :lastName, \s
                phone   = :contact.phone,\s
                address = :location.address,\s
                city    = :location.city,\s
                state   = :location.state,\s
                zip     = :location.zip
   \s""";

    @Value("${source.input.file.csv}")
    private Resource customerInputResource;

    private final static String jobName = "load-customer";

    @Bean
    public Step loadCustomerStep(ItemReader<Customer> customerItemReader,
                                 ItemProcessor<Customer, Customer> customerItemProcessor,
                                 ItemWriter<Customer> customerItemWriter,
                                 JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("loadCustomerStep",jobRepository)
                .<Customer,Customer> chunk(chunkSize,transactionManager)
                .reader(customerItemReader)
                .processor(customerItemProcessor)
                .writer(customerItemWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> reader(CustomerFieldMapper mapper) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(customerInputResource)
                .delimited()
                .names("id","firstName", "lastName","email"
                        ,"phone","address","city","state"
                        ,"zip"
                )
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> writer(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Customer>()
                .sql(saveSql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JobLauncher batchJobLauncher(@Qualifier("jobRepository") JobRepository jobRepository,
                                        TaskExecutor taskExecutor) {
        var jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        return jobLauncher;
    }

    @Bean
    public Job job(JobRepository jobRepository,
                   Step step){

        return new JobBuilder(jobName+System.currentTimeMillis(),jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step).end().build();
    }

    @Bean
    JobRepository jobRepository()
    {
        //return an in-memory job repository
        return new ResourcelessJobRepository();

    }
}
