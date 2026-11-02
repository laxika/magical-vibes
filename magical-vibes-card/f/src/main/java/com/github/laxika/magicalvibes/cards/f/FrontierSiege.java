package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "131")
@CardRegistration(set = "TDC", collectorNumber = "256")
@CardRegistration(set = "C17", collectorNumber = "150")
@CardRegistration(set = "SCD", collectorNumber = "184")
public class FrontierSiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public FrontierSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new ConditionalEffect(
                        new SourceHasChosenMode(KHANS),
                        new AwardManaEffect(ManaColor.GREEN, 2)));
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new ConditionalEffect(
                        new SourceHasChosenMode(KHANS),
                        new AwardManaEffect(ManaColor.GREEN, 2)));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasKeywordPredicate(Keyword.FLYING),
                                new ConditionalEffect(
                                        new SourceHasChosenMode(DRAGONS),
                                        new MayEffect(
                                                new EnteringCreatureFightsTargetCreatureEffect(),
                                                "Have that creature fight target creature you don't control?"))));
    }
}
