package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "128")
public class RustedSlasher extends Card {

    public RustedSlasher() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false), new RegenerateEffect()),
                "Sacrifice an artifact: Regenerate Rusted Slasher."
        ));
    }
}
