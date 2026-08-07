package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "64")
@CardRegistration(set = "8ED", collectorNumber = "97")
public class SageOfLatNam extends Card {

    public SageOfLatNam() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false), new DrawCardEffect()),
                "{T}, Sacrifice an artifact: Draw a card."
        ));
    }
}
