package at.chille.crawler.database.repository.sslchecker;

import org.springframework.data.repository.PagingAndSortingRepository;
import at.chille.crawler.database.model.sslchecker.*;

import javax.inject.Named;

@Named("hostSslInfoRepository")
public interface HostSslInfoRepository extends
		PagingAndSortingRepository<HostSslInfo, Long> {

}