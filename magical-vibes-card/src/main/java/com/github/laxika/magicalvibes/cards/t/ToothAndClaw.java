package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "210")
public class ToothAndClaw extends Card {

    public ToothAndClaw() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new CreateTokenEffect(
                                "Carnivore", 3, 1, CardColor.RED, List.of(CardSubtype.BEAST), Set.of(), Set.of())
                ),
                "Sacrifice two creatures: Create a 3/1 red Beast creature token named Carnivore."
        ));
    }
}
