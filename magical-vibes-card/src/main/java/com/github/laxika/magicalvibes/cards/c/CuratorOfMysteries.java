package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "49")
public class CuratorOfMysteries extends Card {

    public CuratorOfMysteries() {
        // Whenever you cycle or discard another card, scry 1. Cycling is a discard (CR 702.29e), so a
        // single "controller discards" trigger covers both wordings. The source is on the battlefield
        // while any discarded card comes from hand, so the discard is always "another card".
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new ScryEffect(1));

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {U} ({U}, Discard this card: Draw a card.)"));
    }
}
