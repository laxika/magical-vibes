package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringLandTappedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "20")
@CardRegistration(set = "DMC", collectorNumber = "96")
public class TillerEngine extends Card {

    public TillerEngine() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new EnteringLandTappedConditionalEffect(
                        new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Untap that land.",
                                        new UntapTriggeringPermanentEffect()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Tap target nonland permanent an opponent controls.",
                                        new TapPermanentsEffect(TapUntapScope.TARGET),
                                        TargetFilters.nonlandPermanentAnOpponentControls())
                        )))));
    }
}
