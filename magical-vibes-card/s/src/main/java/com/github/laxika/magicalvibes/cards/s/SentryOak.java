package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "LRW", collectorNumber = "38")
public class SentryOak extends Card {

    public SentryOak() {
        // Defender comes from Scryfall keywords.
        // At the beginning of combat on your turn, you may clash with an opponent.
        // If you win, this creature gets +2/+0 and loses defender until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(
                        new ClashEffect(SequenceEffect.of(
                                new BoostSelfEffect(2, 0),
                                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF))),
                        "Clash with an opponent?"));
    }
}
