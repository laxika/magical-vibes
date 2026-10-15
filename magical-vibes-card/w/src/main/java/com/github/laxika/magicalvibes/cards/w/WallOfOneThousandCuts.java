package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "36")
public class WallOfOneThousandCuts extends Card {

    public WallOfOneThousandCuts() {
        // Defender and flying are auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(new CanAttackAsThoughNoDefenderEffect()),
                "{W}: This creature can attack this turn as though it didn't have defender."));
    }
}
