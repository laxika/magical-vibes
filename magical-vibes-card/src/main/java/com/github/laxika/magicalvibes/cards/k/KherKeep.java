package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "275")
public class KherKeep extends Card {

    public KherKeep() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}{R}, {T}: Create a 0/1 red Kobold creature token named Kobolds of Kher Keep.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new CreateTokenEffect("Kobolds of Kher Keep", 0, 1, CardColor.RED,
                        List.of(CardSubtype.KOBOLD), Set.of(), Set.of())),
                "{1}{R}, {T}: Create a 0/1 red Kobold creature token named Kobolds of Kher Keep."
        ));
    }
}
