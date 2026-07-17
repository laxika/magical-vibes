package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "351")
public class BarbedSextant extends Card {

    public BarbedSextant() {
        // "{1}, {T}, Sacrifice this artifact: Add one mana of any color. Draw a card at the beginning of the next turn's upkeep."
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect(),
                        new RegisterDrawCardsAtNextUpkeepEffect()
                ),
                "{1}, {T}, Sacrifice Barbed Sextant: Add one mana of any color. Draw a card at the beginning of the next turn's upkeep."
        ));
    }
}
