package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GN3", collectorNumber = "2")
public class MaeveInsidiousSinger extends Card {

    public MaeveInsidiousSinger() {
        // {2}{U}: Goad target creature. Whenever that creature attacks one of your opponents this turn, you draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new GoadTargetCreatureUntilNextTurnEffect(),
                        new RegisterDelayedWatchedCreatureAttackEffect(List.of(new DrawCardEffect(1)))
                ),
                "{2}{U}: Goad target creature. Whenever that creature attacks one of your opponents this turn, you draw a card.",
                TargetFilters.creature()
        ));
    }
}
