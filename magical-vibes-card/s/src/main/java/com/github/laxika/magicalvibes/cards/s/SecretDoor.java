package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "71")
public class SecretDoor extends Card {

    public SecretDoor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new VentureIntoDungeonEffect()),
                "{4}{U}: Venture into the dungeon. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
