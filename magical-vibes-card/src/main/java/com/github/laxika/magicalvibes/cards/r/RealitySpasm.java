package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "81")
public class RealitySpasm extends Card {

    public RealitySpasm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                ChooseOneEffect.ChooseOneOption.exactlyXTargets(
                        "Tap X target permanents",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.permanent(), 100),
                ChooseOneEffect.ChooseOneOption.exactlyXTargets(
                        "Untap X target permanents",
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.permanent(), 100)
        )));
    }
}
