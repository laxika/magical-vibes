package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "113")
public class TowerOfCoireall extends Card {

    public TowerOfCoireall() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                        "non-Wall creatures")),
                "{T}: Target creature can't be blocked by Walls this turn.",
                TargetFilters.creature()));
    }
}
