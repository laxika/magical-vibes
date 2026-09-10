package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "107")
public class MagmaSliver extends Card {

    public MagmaSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        PermanentAllOfPredicate sliverCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                sliver
        ));
        PermanentCount sliverCount = new PermanentCount(sliver, CountScope.ANY_PLAYER);

        ActivatedAbility boostAbility = new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(sliverCount, new Fixed(0), sliverCreature)),
                "{T}: Target Sliver creature gets +X/+0 until end of turn, where X is the number of Slivers on the battlefield.",
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
