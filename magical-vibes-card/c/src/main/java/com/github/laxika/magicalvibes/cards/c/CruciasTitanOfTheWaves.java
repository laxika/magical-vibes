package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToHandWithRelativeManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "18")
public class CruciasTitanOfTheWaves extends Card {

    public CruciasTitanOfTheWaves() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                DiscardCardThenEffect.continuing(
                        null,
                        SequenceEffect.of(
                                CreateTokenEffect.ofTreasureToken(1),
                                new ChooseOneEffect(List.of(
                                        new ChooseOneEffect.ChooseOneOption(
                                                "Ambitious",
                                                new SeekFromLibraryToHandWithRelativeManaValueEffect(
                                                        new LastDiscardedCardManaValue(), true)),
                                        new ChooseOneEffect.ChooseOneOption(
                                                "Expedient",
                                                new SeekFromLibraryToHandWithRelativeManaValueEffect(
                                                        new LastDiscardedCardManaValue(), false))))),
                        "a card"),
                "Discard a card?"));
    }
}
