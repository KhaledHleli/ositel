package org.ositel.technical_test.repository;

import org.ositel.technical_test.domain.XlsxFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data JPA repository for the XlsxFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface XlsxFileRepository extends JpaRepository<XlsxFile, Long> {
    XlsxFile findByFileName(String fileName);
}
