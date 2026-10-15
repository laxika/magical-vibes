package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;

@CardRegistration(set = "WHO", collectorNumber = "566")
public class AmysHome extends Card {

    public AmysHome() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new MayEffect(
                new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(),
                "Exile a nonland card from your hand?"));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(),
                "Exile a nonland card from your hand?"));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new TimeTravelEffect(1));
    }
}
