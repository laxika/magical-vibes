package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "3")
public class AngelsGrace extends Card {

    public AngelsGrace() {
        addEffect(EffectSlot.SPELL, new GrantPlayerStaticEffectsUntilEndOfTurnEffect(List.of(
                new CantLoseGameEffect(),
                new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS))));
    }
}
