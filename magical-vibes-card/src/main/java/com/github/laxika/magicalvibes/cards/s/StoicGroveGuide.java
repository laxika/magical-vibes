package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "243")
public class StoicGroveGuide extends Card {

    public StoicGroveGuide() {
        // {1}{B/G}, Exile this card from your graveyard: Create a 2/2 black and green Elf creature token.
        // Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B/G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect("Elf", 2, 2, CardColor.BLACK,
                                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.ELF))
                ),
                "{1}{B/G}, Exile this card from your graveyard: Create a 2/2 black and green Elf creature token. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
