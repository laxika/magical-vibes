package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "166")
public class AntQueen extends Card {

    public AntQueen() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new CreateTokenEffect(
                        "Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT),
                        Set.of(),
                        Set.of()
                )),
                "{1}{G}: Create a 1/1 green Insect creature token."
        ));
    }
}
