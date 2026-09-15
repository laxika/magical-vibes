package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "221")
public class BirthingBoughs extends Card {

    public BirthingBoughs() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenEffect(
                        "Shapeshifter", 2, 2, null,
                        List.of(CardSubtype.SHAPESHIFTER), Set.of(Keyword.CHANGELING), Set.of())),
                "{4}, {T}: Create a 2/2 colorless Shapeshifter creature token with changeling."
        ));
    }
}
