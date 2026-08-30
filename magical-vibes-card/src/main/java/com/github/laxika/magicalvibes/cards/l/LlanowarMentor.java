package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "131")
public class LlanowarMentor extends Card {

    public LlanowarMentor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Llanowar Elves",
                                1,
                                1,
                                CardColor.GREEN,
                                null,
                                List.of(CardSubtype.ELF, CardSubtype.DRUID),
                                Set.of(),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(ManaAbilities.tapFor(ManaColor.GREEN)),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{G}, {T}, Discard a card: Create a 1/1 green Elf Druid creature token named Llanowar Elves. It has \"{T}: Add {G}.\""
        ));
    }
}
