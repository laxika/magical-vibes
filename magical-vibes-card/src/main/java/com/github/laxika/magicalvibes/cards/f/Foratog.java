package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "249")
public class Foratog extends Card {

    public Foratog() {
        // {G}, Sacrifice a Forest: This creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest"
                        ),
                        new BoostSelfEffect(2, 2)
                ),
                "{G}, Sacrifice a Forest: This creature gets +2/+2 until end of turn."
        ));
    }
}
