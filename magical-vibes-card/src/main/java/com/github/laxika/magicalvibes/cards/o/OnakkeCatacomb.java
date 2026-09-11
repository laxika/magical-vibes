package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "OPC2", collectorNumber = "28")
public class OnakkeCatacomb extends Card {

    public OnakkeCatacomb() {
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLACK, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new BoostAllOwnCreaturesEffect(1, 0));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES));
    }
}
