package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "131")
public class FrontierSiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public FrontierSiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(
                        new SourceHasChosenMode(KHANS),
                        new AwardManaEffect(ManaColor.GREEN, 2)));
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(
                        new SourceHasChosenMode(KHANS),
                        new AwardManaEffect(ManaColor.GREEN, 2)));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasKeywordPredicate(Keyword.FLYING),
                                new ConditionalEffect(
                                        new SourceHasChosenMode(DRAGONS),
                                        new MayEffect(
                                                new EnteringCreatureFightsTargetCreatureEffect(),
                                                "Have that creature fight target creature you don't control?"))));
    }
}
