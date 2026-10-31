package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "CHR", collectorNumber = "57")
@CardRegistration(set = "ATQ", collectorNumber = "29")
@CardRegistration(set = "ME4", collectorNumber = "142")
public class ArgothianPixies extends Card {

    public ArgothianPixies() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));
        addStaticEffect(new PreventDamageToSelfFromCreaturesEffect(new PermanentIsArtifactPredicate()));
    }
}
