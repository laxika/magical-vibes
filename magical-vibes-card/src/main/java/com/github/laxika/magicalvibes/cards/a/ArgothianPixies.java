package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "CHR", collectorNumber = "57")
public class ArgothianPixies extends Card {

    public ArgothianPixies() {
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.STATIC,
                new PreventDamageToSelfFromCreaturesEffect(new PermanentIsArtifactPredicate()));
    }
}
