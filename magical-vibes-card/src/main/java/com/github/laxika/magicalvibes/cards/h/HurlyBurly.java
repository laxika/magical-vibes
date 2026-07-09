package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "177")
public class HurlyBurly extends Card {

    public HurlyBurly() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Hurly-Burly deals 1 damage to each creature without flying",
                        new MassDamageEffect(1, false, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                new ChooseOneEffect.ChooseOneOption("Hurly-Burly deals 1 damage to each creature with flying",
                        new MassDamageEffect(1, false, false,
                                new PermanentHasKeywordPredicate(Keyword.FLYING)))
        )));
    }
}
