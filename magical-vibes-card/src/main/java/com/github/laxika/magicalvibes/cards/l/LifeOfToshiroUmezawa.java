package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MemoryOfToshiro;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "108")
public class LifeOfToshiroUmezawa extends Card {

    public LifeOfToshiroUmezawa() {
        setBackFaceCard(new MemoryOfToshiro());

        addEffect(EffectSlot.SAGA_CHAPTER_I, chapterChoice());
        addEffect(EffectSlot.SAGA_CHAPTER_II, chapterChoice());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
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
