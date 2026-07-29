package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "246")
public class TropicalStorm extends Card {

    public TropicalStorm() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(0, true, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new Fixed(1), false, false, new PermanentColorInPredicate(Set.of(CardColor.BLUE))));
    }
}
