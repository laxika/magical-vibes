package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MemoryOfToshiro;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "108")
public class LifeOfToshiroUmezawa extends Card {

    public LifeOfToshiroUmezawa() {
        setBackFaceCard(new MemoryOfToshiro());

        addTrigger(Triggers.chapter(1), chapterChoice());
        addTrigger(Triggers.chapter(2), chapterChoice());
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    private ChooseOneAtTriggerTimeEffect chapterChoice() {
        return new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+2 until end of turn.",
                        new BoostTargetCreatureEffect(2, 2), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -1/-1 until end of turn.",
                        new BoostTargetCreatureEffect(-1, -1), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("You gain 2 life.", new GainLifeEffect(2))
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MemoryOfToshiro";
    }
}
