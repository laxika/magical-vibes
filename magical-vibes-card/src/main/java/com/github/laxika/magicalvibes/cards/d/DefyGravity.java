package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "38")
public class DefyGravity extends Card {

    public DefyGravity() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        addCastingOption(new FlashbackCast("{U}"));
    }
}
