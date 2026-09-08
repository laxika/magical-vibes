package com.github.laxika.magicalvibes.cards.l;

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

@CardRegistration(set = "MKM", collectorNumber = "91")
public class LeeringOnlooker extends Card {

    public LeeringOnlooker() {
        // {2}{B}{B}, Exile this card from your graveyard: Create two tapped 1/1 black Bat creature tokens with flying.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(2, "Bat", 1, 1, CardColor.BLACK,
                                List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of(), true)
                ),
                "{2}{B}{B}, Exile this card from your graveyard: Create two tapped 1/1 black Bat creature tokens with flying."
        ));
    }
}
