package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "VIS", collectorNumber = "108")
@CardRegistration(set = "MMQ", collectorNumber = "249")
public class GiantCaterpillar extends Card {

    public GiantCaterpillar() {
        // {G}, Sacrifice this creature: Create a 1/1 green Insect creature token with flying
        // named Butterfly at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                false, "{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new RegisterDelayedCreateTokenEffect(
                                new CreateTokenEffect("Butterfly", 1, 1, CardColor.GREEN,
                                        List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING), Set.of()))
                ),
                "{G}, Sacrifice this creature: Create a 1/1 green Insect creature token with flying "
                        + "named Butterfly at the beginning of the next end step."
        ));
    }
}
