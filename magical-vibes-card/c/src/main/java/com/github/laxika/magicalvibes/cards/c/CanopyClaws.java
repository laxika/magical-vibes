package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "108")
public class CanopyClaws extends Card {

    public CanopyClaws() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        addCastingOption(new FlashbackCast("{G}"));
    }
}
