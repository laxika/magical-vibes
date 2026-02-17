package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

public class TheHive extends Card {

    public TheHive() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateCreatureTokenEffect(
                        "Wasp", 1, 1, null,
                        List.of(CardSubtype.INSECT),
                        Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)
                )),
                false,
                "{5}, {T}: Create a 1/1 colorless Insect artifact creature token with flying named Wasp."
        ));
    }
}
