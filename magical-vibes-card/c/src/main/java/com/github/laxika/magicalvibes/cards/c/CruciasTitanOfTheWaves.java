package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "18")
public class CruciasTitanOfTheWaves extends Card {

    public CruciasTitanOfTheWaves() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                SequenceEffect.of(
                                        CreateTokenEffect.ofTreasureToken(1),
                                        new ChooseOneEffect(List.of(
                                                new ChooseOneEffect.ChooseOneOption(
                                                        "Ambitious",
                                                        new SeekLibraryEffect(1, new CardNotPredicate(
                                                                new CardManaValueAtMostAmountPredicate(
                                                                        new LastDiscardedCardManaValue())))),
                                                new ChooseOneEffect.ChooseOneOption(
                                                        "Expedient",
                                                        new SeekLibraryEffect(1,
                                                                new CardManaValueAtMostAmountPredicate(new Sum(
                                                                        new LastDiscardedCardManaValue(),
                                                                        new Fixed(-1)))))))))),
                "Discard a card?"));
    }
}
