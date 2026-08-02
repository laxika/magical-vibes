package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "201")
public class SoulOfZendikar extends Card {

    public SoulOfZendikar() {
        // {3}{G}{G}: Create a 3/3 green Beast creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(beastToken()),
                "{3}{G}{G}: Create a 3/3 green Beast creature token."
        ));

        // {3}{G}{G}, Exile this card from your graveyard: Create a 3/3 green Beast creature token.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(new ExileSelfFromGraveyardCost(), beastToken()),
                "{3}{G}{G}, Exile this card from your graveyard: Create a 3/3 green Beast creature token."
        ));
    }

    private static CardEffect beastToken() {
        return new CreateTokenEffect("Beast", 3, 3,
                CardColor.GREEN, List.of(CardSubtype.BEAST),
                Set.of(), Set.of());
    }
}
