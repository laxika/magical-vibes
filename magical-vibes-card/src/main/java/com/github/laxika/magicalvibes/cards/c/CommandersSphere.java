package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "203")
@CardRegistration(set = "SLD", collectorNumber = "315")
@CardRegistration(set = "C14", collectorNumber = "54")
@CardRegistration(set = "ECC", collectorNumber = "139")
public class CommandersSphere extends Card {

    public CommandersSphere() {
        // {T}: Add one mana of any color in your commander's color identity.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)),
                "{T}: Add one mana of any color in your commander's color identity."
        ));
        // Sacrifice this artifact: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "Sacrifice this artifact: Draw a card."
        ));
    }
}
