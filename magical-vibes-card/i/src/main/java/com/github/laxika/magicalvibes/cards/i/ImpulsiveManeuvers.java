package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "197")
public class ImpulsiveManeuvers extends Card {

    public ImpulsiveManeuvers() {
        addTrigger(Triggers.creatureAttacks(), new FlipCoinWinEffect(
                PreventNextDamageByTargetCreatureEffect.doubling(Subjects.triggering()).combatDamageOnly(),
                PreventNextDamageByTargetCreatureEffect.preventing(Subjects.triggering()).combatDamageOnly()));
    }
}
