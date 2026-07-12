package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "83")
public class BloodmarkMentor extends Card {

    public BloodmarkMentor() {
        // Bloodmark Mentor is itself red, so ALL_OWN_CREATURES (source must pass the filter too).
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
    }
}
