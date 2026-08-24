package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "182")
public class HighAlert extends Card {

    public HighAlert() {
        addEffect(EffectSlot.STATIC,
                new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC,
                new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentIsCreaturePredicate()))));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{W}{U}: Untap target creature.",
                TargetFilters.creature()));
    }
}
