package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "223")
public class BorisDevilboon extends Card {

    public BorisDevilboon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{R}",
                List.of(new CreateTokenEffect(
                        1, "Minor Demon", 1, 1,
                        CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.RED),
                        List.of(CardSubtype.DEMON))),
                "{2}{B}{R}, {T}: Create a 1/1 black and red Demon creature token named Minor Demon."
        ));
    }
}
