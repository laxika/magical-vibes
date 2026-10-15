package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "137")
public class PossessedCentaur extends Card {

    public PossessedCentaur() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new StaticBoostEffect(1, 1, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantColorEffect(CardColor.BLACK, GrantScope.SELF, true)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                "{2}{B}",
                                List.of(new DestroyTargetPermanentEffect()),
                                "{2}{B}, {T}: Destroy target green creature.",
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentColorInPredicate(Set.of(CardColor.GREEN))
                                        )),
                                        "Target must be a green creature."
                                )
                        ),
                        GrantScope.SELF)));
    }
}
