package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "160")
public class ScrapyardSteelbreaker extends Card {

    public ScrapyardSteelbreaker() {
        // {1}, Sacrifice another artifact: This creature gets +2/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "another artifact"),
                        new BoostSelfEffect(2, 1)
                ),
                "{1}, Sacrifice another artifact: This creature gets +2/+1 until end of turn."
        ));
    }
}
