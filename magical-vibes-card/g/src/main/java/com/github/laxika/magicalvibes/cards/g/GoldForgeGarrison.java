package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class GoldForgeGarrison extends Card {

    public GoldForgeGarrison() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2)),
                "{T}: Add two mana of any one color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenEffect(
                        "Golem",
                        4,
                        4,
                        null,
                        List.of(CardSubtype.GOLEM),
                        Set.of(),
                        Set.of(CardType.ARTIFACT)
                )),
                "{4}, {T}: Create a 4/4 colorless Golem artifact creature token."
        ));
    }
}
