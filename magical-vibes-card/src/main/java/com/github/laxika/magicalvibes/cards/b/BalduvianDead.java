package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "43")
public class BalduvianDead extends Card {

    public BalduvianDead() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                1, "Graveborn", 3, 1,
                                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.RED),
                                List.of(CardSubtype.GRAVEBORN),
                                Set.of(Keyword.HASTE), Set.of()
                        ),
                        new SacrificeCreatedPermanentsAtEndStepEffect()
                ),
                "{2}{R}, Exile a creature card from your graveyard: Create a 3/1 black and red Graveborn "
                        + "creature token with haste. Sacrifice it at the beginning of the next end step."
        ));
    }
}
