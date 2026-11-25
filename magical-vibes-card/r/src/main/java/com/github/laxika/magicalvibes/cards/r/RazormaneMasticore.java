package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "340")
@CardRegistration(set = "5DN", collectorNumber = "146")
@CardRegistration(set = "DDF", collectorNumber = "56")
public class RazormaneMasticore extends Card {

    public RazormaneMasticore() {
        // First strike is auto-loaded from Scryfall

        // At the beginning of your upkeep, sacrifice this creature unless you discard a card.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(new DiscardEffect(1, Subjects.you()),
                "Discard a card? If you don't, sacrifice this creature.",
                new SacrificeSelfEffect()));

        // At the beginning of your draw step, you may have this creature deal 3 damage to target creature.
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new MayEffect(
                DealDamageEffect.toTargetCreature(3),
                "Deal 3 damage to target creature?"
        ));
    }
}
