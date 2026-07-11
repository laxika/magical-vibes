package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfAndLoseKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "LRW", collectorNumber = "38")
public class SentryOak extends Card {

    public SentryOak() {
        // Defender comes from Scryfall keywords.
        // At the beginning of combat on your turn, you may clash with an opponent.
        // If you win, this creature gets +2/+0 and loses defender until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(
                        new ClashEffect(new BoostSelfAndLoseKeywordEffect(2, 0, Keyword.DEFENDER)),
                        "Clash with an opponent?"));
    }
}
