package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "91")
@CardRegistration(set = "SCD", collectorNumber = "51")
public class FaerieFormation extends Card {

    public FaerieFormation() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new CreateTokenEffect(
                                "Faerie", 1, 1, CardColor.BLUE,
                                List.of(CardSubtype.FAERIE), Set.of(Keyword.FLYING), Set.of()),
                        new DrawCardEffect()),
                "{3}{U}: Create a 1/1 blue Faerie creature token with flying. Draw a card."
        ));
    }
}
