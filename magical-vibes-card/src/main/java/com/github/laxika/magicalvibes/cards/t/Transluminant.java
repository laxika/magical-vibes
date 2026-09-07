package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "186")
public class Transluminant extends Card {

    public Transluminant() {
        // {W}, Sacrifice this creature: Create a 1/1 white Spirit creature token with flying
        // at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                false, "{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new RegisterDelayedCreateTokenEffect(
                                new CreateTokenEffect("Spirit", 1, 1, CardColor.WHITE,
                                        List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()))
                ),
                "{W}, Sacrifice this creature: Create a 1/1 white Spirit creature token with flying "
                        + "at the beginning of the next end step."
        ));
    }
}
