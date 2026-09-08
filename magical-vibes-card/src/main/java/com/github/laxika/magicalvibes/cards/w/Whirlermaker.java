package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "239")
public class Whirlermaker extends Card {

    public Whirlermaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenEffect(
                        "Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)
                )),
                "{4}, {T}: Create a 1/1 colorless Thopter artifact creature token with flying."
        ));
    }
}
