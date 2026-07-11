package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "PTK", collectorNumber = "32")
public class ZhangFeiFierceWarrior extends Card {

    public ZhangFeiFierceWarrior() {
        // Vanilla 4/4 with vigilance and horsemanship — both keywords are auto-loaded from
        // Scryfall and enforced by the engine (vigilance in attacker declaration, horsemanship
        // in the blocking rules).
    }
}
