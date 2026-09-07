package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "251")
public class CentaurGlade extends Card {

    public CentaurGlade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new CreateTokenEffect("Centaur", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.CENTAUR), Set.of(), Set.of())),
                "{2}{G}{G}: Create a 3/3 green Centaur creature token."
        ));
    }
}
