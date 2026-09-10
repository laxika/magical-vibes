package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "212")
public class WildShape extends Card {

    public WildShape() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "1/3 Turtle with hexproof",
                        List.of(
                                new SetBasePowerToughnessEffect(1, 3),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.TURTLE),
                                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "1/5 Spider with reach",
                        List.of(
                                new SetBasePowerToughnessEffect(1, 5),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.SPIDER),
                                new GrantKeywordEffect(Keyword.REACH, GrantScope.TARGET)),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "3/3 Elephant with trample",
                        List.of(
                                new SetBasePowerToughnessEffect(3, 3),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.ELEPHANT),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        TargetFilters.creatureYouControl())
        )));
    }
}
