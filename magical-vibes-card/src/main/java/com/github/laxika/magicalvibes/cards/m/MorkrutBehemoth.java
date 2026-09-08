package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MID", collectorNumber = "114")
public class MorkrutBehemoth extends Card {

    public MorkrutBehemoth() {
        // As an additional cost to cast this spell, sacrifice a creature or pay {1}{B}.
        addEffect(EffectSlot.SPELL, new SacrificePermanentOrPayManaCost(
                "{1}{B}", new PermanentIsCreaturePredicate(), "a creature"));
    }
}
