package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FUT", collectorNumber = "13")
public class MarshalingCry extends Card {

    public MarshalingCry() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
        addCycling("{2}");
        addCastingOption(new FlashbackCast("{3}{W}"));
    }
}
