package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "238")
public class FirewakeSliver extends Card {

    public FirewakeSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        PermanentAllOfPredicate sliverCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                sliver
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE,
                GrantScope.ALL_CREATURES_INCLUDING_SELF, sliver));

        ActivatedAbility boostAbility = new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(2, 2, sliverCreature)),
                "{1}, Sacrifice this permanent: Target Sliver creature gets +2/+2 until end of turn.",
                new PermanentPredicateTargetFilter(sliverCreature, "Target must be a Sliver creature")
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                boostAbility,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                boostAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
