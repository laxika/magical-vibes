package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "25")
public class MagneticFlux extends Card {

    public MagneticFlux() {
        addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES, new PermanentIsArtifactPredicate()));
    }
}
