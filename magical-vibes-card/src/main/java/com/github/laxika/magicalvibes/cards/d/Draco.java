package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "3")
public class Draco extends Card {

    public Draco() {
        // Domain — This spell costs {2} less to cast for each basic land type among lands you control.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Scaled(new BasicLandTypesAmongControlledLands(), 2)));

        // Domain — At the beginning of your upkeep, sacrifice this creature unless you pay {10}.
        // This cost is reduced by {2} for each basic land type among lands you control.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{10}", new Scaled(new BasicLandTypesAmongControlledLands(), 2)),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
