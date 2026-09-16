package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "24")
public class FlamewarBrashVeteran extends Card {

    public FlamewarBrashVeteran() {
        setBackFaceCard(new FlamewarStreetwiseOperative());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{B}{R}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new TransformSelfEffect()
                ),
                "Sacrifice another artifact: Put a +1/+1 counter on Flamewar and convert it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardHandCost(),
                        new PutAllCardsExiledWithSourceIntoOwnersHandsEffect(true, true)
                ),
                "{1}, Discard your hand: Put all exiled cards you own with intel counters on them into your hand."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "FlamewarStreetwiseOperative";
    }
}
