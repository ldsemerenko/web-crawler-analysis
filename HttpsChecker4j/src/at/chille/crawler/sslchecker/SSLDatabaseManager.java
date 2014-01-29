package at.chille.crawler.sslchecker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import at.chille.crawler.database.model.*;
import at.chille.crawler.database.model.sslchecker.*;
import at.chille.crawler.database.repository.*;
import at.chille.crawler.database.repository.sslchecker.*;

/**
 * Database Manager for HttpsChecker4j
 * 
 * @author chille
 * 
 */
@Component
public class SSLDatabaseManager
{
  private static ClassPathXmlApplicationContext context = null;
  private static SSLDatabaseManager                _instance;

  protected CrawlingSession		 	  currentCrawlingSession;
  protected SslSession                     currentSslSession;
  
  public static SSLDatabaseManager getInstance()
  {
    if (_instance == null)
    {
      _instance = SSLDatabaseManager.getContext().getBean(
          SSLDatabaseManager.class);
    }
    return _instance;
  }
  
  
  
  public synchronized HostSslInfo saveHostInfo(HostSslInfo hi)
  {
    // Reminder: Double store, because by saving the object changes.
    // if it is not restored, it is saved again, and all certificates
    // occur twice in the database every time the hostInfo is saved.
	currentSslSession.addHostSslInfo(hi);
    hi = hostSslInfoRepository.save(hi);
    currentSslSession.addHostSslInfo(hi);
    return hi;
  }

  public synchronized void setNewSslSession(String description)
  {
	  currentSslSession = new SslSession();
	  currentSslSession.setDescription(description);
	  currentSslSession.setTimeStarted(new Date().getTime());
      //currentSslSession.save(currentSslSession);
  }

  public void loadLastSslSession()
  {
    long timeStartedMax = 0;
    for (SslSession cs : sslSessionRepository.findAll())
    {
      if (cs.getTimeStarted().longValue() > timeStartedMax)
      {
        timeStartedMax = cs.getTimeStarted().longValue();
        this.currentSslSession = cs;
      }
    }
  }
  
  public void loadLastCrawlingSession()
  {
    long timeStartedMax = 0;
    for (CrawlingSession cs : crawlingSessionRepository.findAll())
    {
      if (cs.getTimeStarted().longValue() > timeStartedMax)
      {
        timeStartedMax = cs.getTimeStarted().longValue();
        this.currentCrawlingSession = cs;
      }
    }
  }

  public SslSession getCurrentSslSession()
  {
    // not synchronized on purpose: not necessary
    return this.currentSslSession;
  }

  public HashMap<String, Lock> lockedHosts = new HashMap<String, Lock>();

  public Lock getHostLock(String hostName)
  {
    if (!lockedHosts.containsKey(hostName))
      lockedHosts.put(hostName, new ReentrantLock());
    return lockedHosts.get(hostName);
  }

  public HostSslInfo getHostSslInfo(String hostName)
  {
    // not synchronized on purpose: not necessary
	HostSslInfo toReturn = currentSslSession.getSslHosts().get(hostName);
    return toReturn;
  }


  public synchronized void addHostSSLInfo(HostSslInfo hostInfo)
  {
	  currentSslSession.addHostSslInfo(hostInfo);
  }

  public Map<String, HostInfo> getAllHosts()
  {
	  return currentCrawlingSession.getHosts();
  }
  
  protected static synchronized ApplicationContext getContext()
  {
    if (context == null)
    {
      context = new ClassPathXmlApplicationContext();
      String[] locations =
      { "classpath*:resthubContext.xml",
          "classpath*:application-context-democlient.xml",
    		  };
      context.getEnvironment().setActiveProfiles("resthub-jpa");
      context.setConfigLocations(locations);

      context.refresh();
    }

    return context;
  }

  
  @Autowired
  HostInfoRepository        hostInfoRepository;
  @Autowired
  CertificateRepository     certificateRepository;
  @Autowired
  PageInfoRepository        pageInfoRepository;
  @Autowired
  CrawlingSessionRepository crawlingSessionRepository;
  @Autowired
  HeaderRepository          headerRepository;

  @Inject
  @Named("hostInfoRepository")
  public void setHostInfoRepository(HostInfoRepository t)
  {
    this.hostInfoRepository = t;
  }

  @Inject
  @Named("certificateRepository")
  public void setCertificateRepository(CertificateRepository t)
  {
    this.certificateRepository = t;
  }

  @Inject
  @Named("pageInfoRepository")
  public void setPageInfoRepository(PageInfoRepository t)
  {
    this.pageInfoRepository = t;
  }

  @Inject
  @Named("crawlingSessionRepository")
  public void setCrawlingSessionRepository(CrawlingSessionRepository t)
  {
    this.crawlingSessionRepository = t;
  }
  
  @Autowired
  HostSslInfoRepository        hostSslInfoRepository;
  @Autowired
  SslSessionRepository         sslSessionRepository;

  public HostSslInfoRepository getHostSSLInfoRepository()
  {
    return hostSslInfoRepository;
  }

  public SslSessionRepository getSslSessionRepository()
  {
    return sslSessionRepository;
  }


  @Inject
  @Named("hostSslInfoRepository")
  public void setHostSslInfoRepository(HostSslInfoRepository t)
  {
    this.hostSslInfoRepository = t;
  }

//  @Autowired
//  HeaderRepository         headerRepository;
  
//  @Inject
//  @Named("headerRepository")
//  public void setHeaderRepository(HeaderRepository t)
//  {
//    this.headerRepository = t;
//  }
  
  @Inject
  @Named("sslSessionRepository")
  public void setSslSessionRepository(SslSessionRepository t)
  {
    this.sslSessionRepository = t;
  }
}