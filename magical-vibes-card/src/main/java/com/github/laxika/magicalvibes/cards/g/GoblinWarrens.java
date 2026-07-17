package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "187")
@CardRegistration(set = "5ED", collectorNumber = "238")
public class GoblinWarrens extends Card {

    public GoblinWarrens() {
        // {2}{R}, Sacrifice two Goblins: Create three 1/1 red Goblin creature tokens.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)),
                        new CreateTokenEffect(3, "Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), Set.of(), Set.of())
                ),
                "{2}{R}, Sacrifice two Goblins: Create three 1/1 red Goblin creature tokens."
        ));
    }
}
