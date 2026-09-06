package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentCountAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "31")
public class Marjhan extends Card {

    public Marjhan() {
        // "This creature doesn't untap during your untap step."
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // "{U}{U}, Sacrifice a creature: Untap this creature. Activate only during your upkeep."
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new SacrificeCreatureCost(), new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{U}{U}, Sacrifice a creature: Untap this creature. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP));

        // "This creature can't attack unless defending player controls an Island."
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"));

        // "{U}{U}: This creature gets -1/-0 until end of turn and deals 1 damage to target
        // attacking creature without flying."
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new BoostSelfEffect(-1, 0), new DealDamageToTargetCreatureEffect(1)),
                "{U}{U}: This creature gets -1/-0 until end of turn and deals 1 damage to target attacking creature without flying.",
                new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                        "Target must be an attacking creature without flying.")));

        // "When you control no Islands, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentControllerControlsPermanentCountAtMostPredicate(
                        0, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                List.of(new SacrificeSelfEffect()),
                "Marjhan's state-triggered ability"));
    }
}
