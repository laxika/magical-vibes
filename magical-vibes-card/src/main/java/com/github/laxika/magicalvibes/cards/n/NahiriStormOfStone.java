package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "233")
public class NahiriStormOfStone extends Card {

    private static final PermanentPredicate TAPPED_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsTappedPredicate()));

    public NahiriStormOfStone() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new ReduceEquipCostEffect(1)));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new DealDamageToTargetCreatureEffect(new XValue())),
                "−X: Nahiri, Storm of Stone deals X damage to target tapped creature.",
                new PermanentPredicateTargetFilter(TAPPED_CREATURE, "Target must be a tapped creature")
        ));
    }
}
