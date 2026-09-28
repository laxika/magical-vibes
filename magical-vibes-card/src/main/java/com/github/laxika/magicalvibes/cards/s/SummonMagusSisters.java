package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "71")
@CardRegistration(set = "FIC", collectorNumber = "200")
public class SummonMagusSisters extends Card {

    public SummonMagusSisters() {
        var targetCreature = TargetFilters.creature();
        var opponentCreature = TargetFilters.creatureAnOpponentControls();

        for (EffectSlot chapter : List.of(
                EffectSlot.SAGA_CHAPTER_I,
                EffectSlot.SAGA_CHAPTER_II,
                EffectSlot.SAGA_CHAPTER_III)) {
            addEffect(chapter, new ChooseOneAtRandomEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(
                            "Combine Powers! — Put three +1/+1 counters on target creature",
                            new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                            targetCreature),
                    new ChooseOneEffect.ChooseOneOption(
                            "Defense! — Put a shield counter on target creature. You gain 3 life",
                            List.of(
                                    new PutCounterOnTargetPermanentEffect(CounterType.SHIELD),
                                    new GainLifeEffect(3)),
                            targetCreature),
                    new ChooseOneEffect.ChooseOneOption(
                            "Fight! — This creature fights up to one target creature an opponent controls",
                            List.of(new SourceFightsTargetCreatureEffect()),
                            opponentCreature, null, 0, 1, false, null)
            )));
        }
    }
}
