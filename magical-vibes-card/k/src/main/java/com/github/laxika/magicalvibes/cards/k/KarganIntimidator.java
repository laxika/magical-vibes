package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "145")
public class KarganIntimidator extends Card {

    public KarganIntimidator() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentHasSubtypePredicate(CardSubtype.COWARD),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR),
                "Cowards can't block Warriors"));

        addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new BoostSelfEffect(1, 1)),
                "This creature gets +1/+1 until end of turn. Activate only once each turn.", 1));

        addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.COWARD)),
                "Target creature becomes a Coward until end of turn. Activate only once each turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "Target Warrior gains trample until end of turn. Activate only once each turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))),
                        "Target must be a Warrior creature")));
    }
}
