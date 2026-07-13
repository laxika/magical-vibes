package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetAllUnblockedCreaturesBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "168")
public class InkfathomWitch extends Card {

    public InkfathomWitch() {
        // Fear is auto-loaded from Scryfall keywords.
        // {2}{U}{B}: Each unblocked creature has base power and toughness 4/1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{B}",
                List.of(new SetAllUnblockedCreaturesBasePowerToughnessEffect(4, 1)),
                "{2}{U}{B}: Each unblocked creature has base power and toughness 4/1 until end of turn."
        ));
    }
}
