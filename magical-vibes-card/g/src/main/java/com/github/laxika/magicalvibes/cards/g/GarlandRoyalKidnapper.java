package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureWhileMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByMonarchPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "442")
public class GarlandRoyalKidnapper extends Card {

    public GarlandRoyalKidnapper() {
        target(new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerBecomesMonarchEffect());

        PermanentPredicate creaturesYouControlButDontOwn = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES,
                creaturesYouControlButDontOwn));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(new CantBeSacrificedEffect(),
                GrantScope.ALL_OWN_CREATURES, creaturesYouControlButDontOwn));

        target(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledByMonarchPredicate())),
                        "Target must be a creature controlled by the monarch"))
                .addEffect(EffectSlot.ON_OPPONENT_BECOMES_MONARCH,
                        new GainControlOfTargetCreatureWhileMonarchEffect());
    }
}
