package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "130")
public class NectarFaerie extends Card {

    public NectarFaerie() {
        addActivatedAbility(new ActivatedAbility(true, "{B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                "{B}, {T}: Target Faerie or Elf gains lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE, CardSubtype.ELF)),
                        "Target must be a Faerie or Elf"
                )));
    }
}
