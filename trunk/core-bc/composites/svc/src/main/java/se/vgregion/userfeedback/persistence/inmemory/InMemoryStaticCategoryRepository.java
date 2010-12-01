package se.vgregion.userfeedback.persistence.inmemory;

import se.vgregion.dao.domain.patterns.repository.inmemory.AbstractInMemoryRepository;
import se.vgregion.userfeedback.domain.StaticCategory;
import se.vgregion.userfeedback.domain.StaticCategoryRepository;

/**
 * This action do that and that, if it has something special it is.
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class InMemoryStaticCategoryRepository extends AbstractInMemoryRepository<StaticCategory, Long> implements StaticCategoryRepository {

    public InMemoryStaticCategoryRepository() {
        init();
    }

    private void init() {
        this.persist(new StaticCategory(StaticCategoryRepository.STATIC_CONTENT_CATEGORY,
                "Webbplatsens innehåll",
                "Saknar innehåll", "Fel innehåll", "Hittar inte information"));
        this.persist(new StaticCategory(StaticCategoryRepository.STATIC_FUNCTION_CATEGORY,
                "Webbplatsens funktion",
                "Sidan finns inte", "Felmeddelande", "Sidan laddas inte", "Förstår inte funktionen"));
        this.persist(new StaticCategory(StaticCategoryRepository.STATIC_OTHER_CATEGORY,
                "Övrigt"));
    }
}
