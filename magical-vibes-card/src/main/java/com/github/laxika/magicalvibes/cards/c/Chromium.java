package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "74")
@CardRegistration(set = "LEG", collectorNumber = "224")
public class Chromium extends Card {

    public Chromium() {
        // At the beginning of your upkeep, sacrifice Chromium unless you pay {W}{U}{B}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{U}{B}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Rampage 2: whenever Chromium becomes blocked, it gets +2/+2 until end of turn for
        // each creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(2));
    }
}
