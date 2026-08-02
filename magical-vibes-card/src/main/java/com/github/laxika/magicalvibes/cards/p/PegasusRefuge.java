package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "35")
public class PegasusRefuge extends Card {

    public PegasusRefuge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                "Pegasus", 1, 1,
                                CardColor.WHITE, List.of(CardSubtype.PEGASUS),
                                Set.of(Keyword.FLYING), Set.of()
                        )
                ),
                "{2}, Discard a card: Create a 1/1 white Pegasus creature token with flying."
        ));
    }
}
