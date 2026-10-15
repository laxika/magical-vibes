package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToHandWithRelativeManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "18")
public class CruciasTitanOfTheWaves extends Card {

    public CruciasTitanOfTheWaves() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(
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
