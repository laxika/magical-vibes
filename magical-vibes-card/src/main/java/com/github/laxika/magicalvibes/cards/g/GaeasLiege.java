package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesForestUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.TrackedLandsBecomeForestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "247")
public class GaeasLiege extends Card {

    public GaeasLiege() {
        // While not attacking: P/T = Forests you control. While attacking: P/T = Forests the
        // defending player controls (CR 613.4a characteristic-defining ability).
        DynamicAmount forests = new IfSourceAttacking(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.DEFENDING_PLAYER),
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(forests, forests));

        // Keeps every land recorded by the ability below a Forest for as long as this creature is
        // on the battlefield.
        addEffect(EffectSlot.STATIC, new TrackedLandsBecomeForestEffect());

        // {T}: Target land becomes a Forest until this creature leaves the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetLandBecomesForestUntilSourceLeavesEffect()),
                "{T}: Target land becomes a Forest until this creature leaves the battlefield.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land")));
    }
}
