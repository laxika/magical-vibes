package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect;

@CardRegistration(set = "PIP", collectorNumber = "69")
@CardRegistration(set = "PIP", collectorNumber = "597")
public class Vault21HouseGambit extends Card {

    public Vault21HouseGambit() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DiscardAndDrawCardEffect(1, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DiscardAndDrawCardEffect(1, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect());
    }
}
