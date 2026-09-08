package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "20")
public class RhysticShield extends Card {

    public RhysticShield() {
        // Creatures you control get +0/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(0, 1));

        // They get an additional +0/+2 until end of turn unless any player pays {2}.
        addEffect(EffectSlot.SPELL, new ForcedCostOrElseEffect(
                new PayManaCost("{2}"),
                List.of(new BoostAllOwnCreaturesEffect(0, 2)),
                true,
                true));
    }
}
