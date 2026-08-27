package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "62")
public class JadedAnalyst extends Card {

    public JadedAnalyst() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new NthCardDrawTriggerEffect(2,
                SequenceEffect.of(
                        new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF))));
    }
}
