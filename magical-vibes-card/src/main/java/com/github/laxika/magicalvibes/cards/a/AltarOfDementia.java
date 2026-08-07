package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "276")
public class AltarOfDementia extends Card {

    public AltarOfDementia() {
        // Sacrifice a creature: Target player mills cards equal to the sacrificed creature's power.
        // The sacrifice is a cost, so it snapshots the creature's effective power into the entry's xValue.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, true),
                        new MillEffect(new XValue(), MillRecipient.TARGET_PLAYER)
                ),
                "Sacrifice a creature: Target player mills cards equal to the sacrificed creature's power."
        ));
    }
}
