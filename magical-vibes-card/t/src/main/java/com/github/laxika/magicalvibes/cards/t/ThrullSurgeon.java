package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "183")
@CardRegistration(set = "EXO", collectorNumber = "76")
@CardRegistration(set = "TPR", collectorNumber = "121")
public class ThrullSurgeon extends Card {

    public ThrullSurgeon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(), new ChooseCardsFromTargetHandEffect(new Fixed(1), List.of(), List.of(),
                        HandChoiceDestination.DISCARD, false, null, 0, false, false, false, false)),
                "{1}{B}, Sacrifice this creature: Look at target player's hand and choose a card from it. That player discards that card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
