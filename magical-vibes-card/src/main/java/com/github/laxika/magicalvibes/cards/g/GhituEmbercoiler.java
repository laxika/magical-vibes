package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryWithGreaterManaValueEffect;

@CardRegistration(set = "YDMU", collectorNumber = "12")
public class GhituEmbercoiler extends Card {

    public GhituEmbercoiler() {
        // At the beginning of your first main phase, you may discard a card. If you do, seek a
        // card with greater mana value and exile it. Until the end of your next turn, you may play
        // the exiled card.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new SeekFromLibraryWithGreaterManaValueEffect(new LastDiscardedCardManaValue()),
                        "a card"),
                "Discard a card to seek a card with greater mana value?"));
    }
}
