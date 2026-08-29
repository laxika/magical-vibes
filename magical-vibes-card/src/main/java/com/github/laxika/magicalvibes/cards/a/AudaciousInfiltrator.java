package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "AER", collectorNumber = "7")
public class AudaciousInfiltrator extends Card {

    public AudaciousInfiltrator() {
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));
    }
}
