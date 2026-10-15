package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MB2", collectorNumber = "330")
@CardRegistration(set = "MB2", collectorNumber = "567")
public class SnapJudgment extends Card {

    public SnapJudgment() {
        // The match-level clause has no stateful equivalent in the single-game engine.
        addCycling("{1}");
    }
}
