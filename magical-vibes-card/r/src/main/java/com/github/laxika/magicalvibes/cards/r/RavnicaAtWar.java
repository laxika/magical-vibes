package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "WAR", collectorNumber = "28")
public class RavnicaAtWar extends Card {

    public RavnicaAtWar() {
        addSpellEffect(ExileTargetPermanentEffect.all(new PermanentIsMulticoloredPredicate()));
    }
}
