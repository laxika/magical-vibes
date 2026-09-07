package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenIfDyingSourceHadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "132")
public class ToxrillTheCorrosive extends Card {

    public ToxrillTheCorrosive() {
        PermanentPredicate opponentsCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.SLIME, 1, opponentsCreatures, EachPermanentScope.ALL_PLAYERS));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(-1, -1, GrantScope.ALL_CREATURES,
                        opponentsCreatures, CounterType.SLIME, true));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new CreateTokenIfDyingSourceHadCounterEffect(
                        CounterType.SLIME,
                        new CreateTokenEffect("Slug", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.SLUG), Set.of(), Set.of())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SLUG),
                                "Sacrifice a Slug", false),
                        new DrawCardEffect()
                ),
                "{U}{B}, Sacrifice a Slug: Draw a card."
        ));
    }
}
