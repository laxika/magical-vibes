package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "109")
public class IllustriousHistorian extends Card {

    public IllustriousHistorian() {
        // {5}, Exile this card from your graveyard: Create a tapped 3/2 red and white Spirit creature token.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Spirit",
                                3,
                                2,
                                CardColor.RED,
                                Set.of(CardColor.RED, CardColor.WHITE),
                                List.of(CardSubtype.SPIRIT),
                                Set.of(),
                                Set.of(),
                                false,
                                true,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                false,
                                0,
                                Set.of()
                        )
                ),
                "{5}, Exile this card from your graveyard: Create a tapped 3/2 red and white Spirit creature token.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
