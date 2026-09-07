package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "37")
public class NahirisResolve extends Card {

    public NahirisResolve() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.OWN_CREATURES));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                FlickerEffect.exileControllersAnyNumberPermanentsReturnAtStep(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate())))),
                        TurnStep.UPKEEP, true));
    }
}
