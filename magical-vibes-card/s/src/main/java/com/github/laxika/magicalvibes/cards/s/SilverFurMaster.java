package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.KeywordCost;
import com.github.laxika.magicalvibes.model.effect.ModifyKeywordCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "236")
public class SilverFurMaster extends Card {

    public SilverFurMaster() {
        addNinjutsu("{U}{B}");

        addStaticEffect(ModifyKeywordCostEffect.reduceYours(KeywordCost.NINJUTSU, 1));
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.NINJA, CardSubtype.ROGUE))));
    }
}
