package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "11")
public class EnvoyOfOkinecAhau extends Card {

    public EnvoyOfOkinecAhau() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new CreateTokenEffect("Gnome", 1, 1, null,
                        List.of(CardSubtype.GNOME), Set.of(), Set.of(CardType.ARTIFACT))),
                "{4}{W}: Create a 1/1 colorless Gnome artifact creature token."
        ));
    }
}
