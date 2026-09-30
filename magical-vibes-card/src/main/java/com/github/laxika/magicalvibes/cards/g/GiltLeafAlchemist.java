package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "14")
public class GiltLeafAlchemist extends Card {

    public GiltLeafAlchemist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConjureCardToBattlefieldEffect("Forest")),
                "{T}: Conjure a card named Forest onto the battlefield. Activate only if two or more Elf cards are in your graveyard."
        ).withRequiredGraveyardCards(
                new CardSubtypePredicate(CardSubtype.ELF), 2, "Elf cards in your graveyard"));
    }
}
