package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "86")
public class ArnimZolaBioFanatic extends Card {

    public ArnimZolaBioFanatic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new CreateTokenEffect(
                        1, "Villain", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of(), true)),
                "{3}, {T}: Create a tapped 2/1 black Villain creature token with menace. Activate only if there are two or more creature cards in your graveyard."
        ).withRequiredGraveyardCards(
                new CardTypePredicate(CardType.CREATURE), 2, "creature cards in your graveyard"));
    }
}
