package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "43")
public class ComponentCollector extends Card {

    public ComponentCollector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE,
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "Tap or untap target nonland permanent?"));
    }
}
