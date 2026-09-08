package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastOneWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MSH", collectorNumber = "245")
public class CosmicCube extends Card {

    public CosmicCube() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new LookAtTopCardsMayCastOneWithoutPayingManaCostEffect(
                        new Fixed(6),
                        new GreatestPowerAmongControlled(new PermanentIsAttackingPredicate())));
    }
}
