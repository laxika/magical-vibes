package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "265")
public class GrimFeast extends Card {

    public GrimFeast() {
        // "At the beginning of your upkeep, this enchantment deals 1 damage to you."
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));

        // "Whenever a creature is put into an opponent's graveyard from the battlefield, you gain
        // life equal to its toughness." The toughness used is the creature's last-known effective
        // toughness on the battlefield, read from the stack entry as the ability resolves.
        addTrigger(Triggers.dies(Triggers.CREATURE, ObjectRelation.OPPONENT_OWNS),
                new GainLifeEffect(new TriggeringPermanentToughness()));
    }
}
