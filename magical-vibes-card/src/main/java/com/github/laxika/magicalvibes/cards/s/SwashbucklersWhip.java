package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "263")
public class SwashbucklersWhip extends Card {

    public SwashbucklersWhip() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.REACH, GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                        "{2}, {T}: Tap target artifact or creature.",
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                "Target must be an artifact or creature"
                        )
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{8}",
                        List.of(new DiscoverEffect(10)),
                        "{8}, {T}: Discover 10."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
