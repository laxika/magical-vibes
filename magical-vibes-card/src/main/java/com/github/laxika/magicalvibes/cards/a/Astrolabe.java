package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "118a")
@CardRegistration(set = "ALL", collectorNumber = "118b")
public class Astrolabe extends Card {

    public Astrolabe() {
        // "{1}, {T}, Sacrifice this artifact: Add two mana of any one color. Draw a card at the beginning of the next turn's upkeep."
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect(2),
                        new RegisterDrawCardsAtNextUpkeepEffect()
                ),
                "{1}, {T}, Sacrifice Astrolabe: Add two mana of any one color. Draw a card at the beginning of the next turn's upkeep."
        ));
    }
}
