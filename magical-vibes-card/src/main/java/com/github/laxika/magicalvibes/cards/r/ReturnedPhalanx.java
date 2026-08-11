package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "104")
public class ReturnedPhalanx extends Card {

    public ReturnedPhalanx() {
        // Defender is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}",
                List.of(new CanAttackAsThoughNoDefenderEffect()),
                "{1}{U}: This creature can attack this turn as though it didn't have defender."));
    }
}
