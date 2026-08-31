package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;

@CardRegistration(set = "FUT", collectorNumber = "23")
public class ImperialMask extends Card {

    public ImperialMask() {
        // "You have hexproof."
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        // The token-copy clause applies only to teammates, and the engine has no team variant.
    }
}
