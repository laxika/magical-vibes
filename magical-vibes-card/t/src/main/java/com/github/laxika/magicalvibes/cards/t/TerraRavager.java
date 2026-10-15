package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "C13", collectorNumber = "126")
public class TerraRavager extends Card {

    public TerraRavager() {
        PermanentCount defendingLands = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.DEFENDING_PLAYER);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(defendingLands, new Fixed(0)));
    }
}
