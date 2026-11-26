package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ICE", collectorNumber = "149")
public class MindWhip extends Card {

    public MindWhip() {
        // Enchant creature. At the beginning of the upkeep of enchanted creature's controller,
        // that player may pay {3}. If they don't, this Aura deals 2 damage to that player and you
        // tap that creature.
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                UnlessPaysEffect.unlessPays(Subjects.activePlayer(), "{3}", SequenceEffect.of(
                        new DealDamageToPlayersEffect(2, Subjects.activePlayer()),
                        new TapPermanentsEffect(Subjects.attachedAtTrigger()))));
    }
}
