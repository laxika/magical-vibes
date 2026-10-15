package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "37")
public class ElvenRaftSteerer extends Card {

    public ElvenRaftSteerer() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Tap target creature an opponent controls.",
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                TargetFilters.creatureAnOpponentControls()),
                        new ChooseOneEffect.ChooseOneOption(
                                "Untap target creature you control.",
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                TargetFilters.creatureYouControl())
                ))));
    }
}
