package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "114")
public class RoughTumble extends Card {

    public RoughTumble() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Rough deals 2 damage to each creature without flying",
                        new MassDamageEffect(2, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))))
                        .withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Tumble deals 6 damage to each creature with flying",
                        new MassDamageEffect(6, false, false,
                                new PermanentHasKeywordPredicate(Keyword.FLYING)))
                        .withManaCost("{5}{R}")
        )));
    }
}
