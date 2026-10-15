package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ODY", collectorNumber = "226")
public class VolcanicSpray extends Card {

    public VolcanicSpray() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new Fixed(1),
                true,
                false,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addCastingOption(new FlashbackCast("{1}{R}"));
    }
}
