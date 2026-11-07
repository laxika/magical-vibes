package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "187")
public class MindbladeRender extends Card {

    public MindbladeRender() {
        // "Whenever your opponents are dealt combat damage, if any of that damage was dealt by a Warrior": any
        // player's Warrior dealing combat damage to an opponent, once for the combat damage step.
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
