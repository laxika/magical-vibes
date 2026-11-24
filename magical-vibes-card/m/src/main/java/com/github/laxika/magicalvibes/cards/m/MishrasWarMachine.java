package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "337")
@CardRegistration(set = "SUM", collectorNumber = "266")
@CardRegistration(set = "ATQ", collectorNumber = "57")
@CardRegistration(set = "3ED", collectorNumber = "266")
public class MishrasWarMachine extends Card {

    public MishrasWarMachine() {
        // At the beginning of your upkeep, this creature deals 3 damage to you unless you discard a
        // card. If it deals damage to you this way, tap it.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(new DiscardEffect(1, Subjects.you()),
                "Discard a card? If you don't, this creature deals 3 damage to you and taps if it does.",
                SequenceEffect.of(new DealDamageToPlayersEffect(3, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toAPlayer(StepKind.DEAL_DAMAGE, PlayerRelation.SELF),
                                new TapPermanentsEffect(Subjects.source())))));
    }
}
