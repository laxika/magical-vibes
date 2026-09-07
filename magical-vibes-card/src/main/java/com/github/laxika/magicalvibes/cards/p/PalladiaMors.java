package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "82")
@CardRegistration(set = "LEG", collectorNumber = "247")
public class PalladiaMors extends Card {

    public PalladiaMors() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{R}{G}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
