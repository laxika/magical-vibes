package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LichDuelMasteryLifeLossReplacementEffect;

@CardRegistration(set = "MB2", collectorNumber = "310")
@CardRegistration(set = "MB2", collectorNumber = "546")
public class LichsDuelMastery extends Card {

    public LichsDuelMastery() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new LichDuelMasteryLifeLossReplacementEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(5, true));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
    }
}
