package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

@CardRegistration(set = "TSP", collectorNumber = "3")
@CardRegistration(set = "MMA", collectorNumber = "4")
@CardRegistration(set = "TSR", collectorNumber = "4")
@CardRegistration(set = "SOA", collectorNumber = "2")
public class AngelsGrace extends Card {

    public AngelsGrace() {
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(new CantLoseGameEffect()));
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                new DamageLifeFloorEffect(1, LifeFloorCondition.LIFE_AT_LEAST_FLOOR)));
    }
}
