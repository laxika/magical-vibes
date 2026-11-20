package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "657")
public class GreenGoblinBackForMore extends Card {

    public GreenGoblinBackForMore() {
        addCastingOption(new GraveyardCast(null, "{3}{B}{B}", List.of(), new CardDiscardedThisTurn()));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new DiscardEffect(1, Subjects.eachOpponent()))),
                "Discard a card?"));
    }
}
