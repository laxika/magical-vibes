package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandBottomThenDamageEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "176")
public class GoblinCharbelcher extends Card {

    public GoblinCharbelcher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new RevealUntilLandBottomThenDamageEffect()),
                "{3}, {T}: Reveal cards from the top of your library until you reveal a land card."
                        + " This artifact deals damage equal to the number of nonland cards revealed this way to any target."
                        + " If the revealed land card was a Mountain, this artifact deals double that damage instead."
                        + " Put the revealed cards on the bottom of your library in any order."
        ));
    }
}
