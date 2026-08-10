package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "122")
public class RootwaterAlligator extends Card {

    public RootwaterAlligator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.FOREST), "Sacrifice a Forest"),
                        new RegenerateEffect()
                ),
                "Sacrifice a Forest: Regenerate Rootwater Alligator."
        ));
    }
}
