package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1201")
public class JeskaThriceReborn extends Card {

    public JeskaThriceReborn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.LOYALTY, new CommanderCastsFromCommandZoneThisGame()));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect(3)),
                "0: Choose target creature. Until your next turn, if that creature would deal combat damage to one of your opponents, it deals triple that damage to that player instead.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new DealDamageToEachTargetEffect(new XValue())),
                "−X: Jeska deals X damage to each of up to three targets.",
                null, 0, null, null, List.of(), 0, 3, true));
    }
}
