package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "25")
public class GovernTheGuildless extends Card {

    public GovernTheGuildless() {
        PermanentAllOfPredicate monocoloredCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsMonocoloredPredicate()
        ));

        target(new PermanentPredicateTargetFilter(monocoloredCreature, "Target must be a monocolored creature"))
                .addEffect(EffectSlot.SPELL,
                        GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT, monocoloredCreature));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new BecomeChosenColorsUntilEndOfTurnEffect()),
                "Forecast — {1}{U}, Reveal this card from your hand: Target creature becomes the color or colors of your choice "
                        + "until end of turn. Activate only during your upkeep and only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
