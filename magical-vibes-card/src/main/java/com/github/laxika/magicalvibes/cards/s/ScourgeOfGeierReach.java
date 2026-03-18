package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "162")
public class ScourgeOfGeierReach extends Card {

    public ScourgeOfGeierReach() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerOpponentPermanentEffect(1, 1, new PermanentIsCreaturePredicate()));
    }
}
