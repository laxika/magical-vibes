package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "301")
public class Metrognome extends Card {

    public Metrognome() {
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, gnomeToken(4));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(gnomeToken(1)),
                "{4}, {T}: Create a 1/1 colorless Gnome artifact creature token."
        ));
    }

    private static CreateTokenEffect gnomeToken(int amount) {
        return new CreateTokenEffect(amount, "Gnome", 1, 1, null,
                List.of(CardSubtype.GNOME), Set.of(), Set.of(CardType.ARTIFACT));
    }
}
