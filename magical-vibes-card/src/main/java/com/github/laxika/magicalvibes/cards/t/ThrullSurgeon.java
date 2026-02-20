package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "183")
public class ThrullSurgeon extends Card {

    public ThrullSurgeon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new ChooseCardFromTargetHandToDiscardEffect(1, List.of())),
                true,
                "Sacrifice Thrull Surgeon: Target player reveals their hand. You choose a card from it. That player discards that card.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
