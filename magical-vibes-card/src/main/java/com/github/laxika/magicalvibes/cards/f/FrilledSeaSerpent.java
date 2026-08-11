package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "56")
@CardRegistration(set = "M20", collectorNumber = "61")
public class FrilledSeaSerpent extends Card {

    public FrilledSeaSerpent() {
        // {5}{U}{U}: Frilled Sea Serpent can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{5}{U}{U}: Frilled Sea Serpent can't be blocked this turn."
        ));
    }
}
