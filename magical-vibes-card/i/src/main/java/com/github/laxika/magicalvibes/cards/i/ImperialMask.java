package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;

@CardRegistration(set = "FUT", collectorNumber = "23")
public class ImperialMask extends Card {

    public ImperialMask() {
        // "You have hexproof."
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        // "Each of your teammates creates a token copy" — the engine has no team games, so there are
        // zero teammates; the nontoken-gated trigger still goes on the stack and creates nothing.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new NotCondition(new SourceIsToken()),
                        new CreateTokenCopyOfSourceEffect(false, 0)));
    }
}
