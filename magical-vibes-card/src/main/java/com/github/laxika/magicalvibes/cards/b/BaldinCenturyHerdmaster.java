package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "428")
@CardRegistration(set = "SLX", collectorNumber = "10")
public class BaldinCenturyHerdmaster extends Card {

    public BaldinCenturyHerdmaster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_CREATURES)));

        target(TargetFilters.creature(), 0, 100).addEffect(EffectSlot.ON_ATTACK,
                new BoostTargetCreatureEffect(new Fixed(0), new CardsInHand(CountScope.CONTROLLER)));
    }
}
