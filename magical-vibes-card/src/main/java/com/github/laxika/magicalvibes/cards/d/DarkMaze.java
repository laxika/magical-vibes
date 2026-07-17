package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "80")
public class DarkMaze extends Card {

    public DarkMaze() {
        // Defender is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new CanAttackAsThoughNoDefenderEffect(), new ExileSelfAtEndStepEffect()),
                "{0}: This creature can attack this turn as though it didn't have defender. Exile it at the beginning of the next end step."));
    }
}
