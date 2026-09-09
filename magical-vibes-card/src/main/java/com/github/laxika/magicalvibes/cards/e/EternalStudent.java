package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "82")
public class EternalStudent extends Card {

    public EternalStudent() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(2, "Inkling", 1, 1,
                                CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.INKLING), Set.of(Keyword.FLYING), Set.of())),
                "{1}{B}, Exile this card from your graveyard: Create two 1/1 white and black Inkling creature tokens with flying."
        ));
    }
}
