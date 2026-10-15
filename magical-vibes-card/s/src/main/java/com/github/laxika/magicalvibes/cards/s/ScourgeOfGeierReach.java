package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "162")
public class ScourgeOfGeierReach extends Card {

    public ScourgeOfGeierReach() {
        // Scourge of Geier Reach gets +1/+1 for each creature your opponents control.
        PermanentCount opponentCreatures =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(opponentCreatures, opponentCreatures));
    }
}
