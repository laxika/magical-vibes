package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentsAttackWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "THB", collectorNumber = "135")
public class FlummoxedCyclops extends Card {

    public FlummoxedCyclops() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentsAttackWithAtLeastCreatures(2),
                        CantBlockThisCombatEffect.self()));
    }
}
