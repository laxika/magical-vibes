package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "AER", collectorNumber = "7")
@CardRegistration(set = "KLR", collectorNumber = "8")
public class AudaciousInfiltrator extends Card {

    public AudaciousInfiltrator() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));
    }
}
