package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "177")
public class HurlyBurly extends Card {

    public HurlyBurly() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Hurly-Burly deals 1 damage to each creature without flying",
                        DealDamageEffect.toEachCreature(1,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                new ChooseOneEffect.ChooseOneOption("Hurly-Burly deals 1 damage to each creature with flying",
                        DealDamageEffect.toEachCreature(1, new PermanentHasKeywordPredicate(Keyword.FLYING)))
        )));
    }
}
