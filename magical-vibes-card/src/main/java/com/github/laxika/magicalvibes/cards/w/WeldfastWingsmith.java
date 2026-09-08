package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "KLD", collectorNumber = "69")
public class WeldfastWingsmith extends Card {

    public WeldfastWingsmith() {
        // Whenever an artifact you control enters, this creature gains flying until end of turn.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
