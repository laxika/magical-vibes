package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromPermanentOrSuspendedCardCost;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "106")
public class RiftElemental extends Card {

    public RiftElemental() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new RemoveTimeCounterFromPermanentOrSuspendedCardCost(),
                        new BoostSelfEffect(2, 0)),
                "{1}{R}, Remove a time counter from a permanent you control or suspended card you own: This creature gets +2/+0 until end of turn."));
    }
}
