package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "41")
public class EtheriumAstrolabe extends Card {

    public EtheriumAstrolabe() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false), new DrawCardEffect(1)),
                "{B}, {T}, Sacrifice an artifact: Draw a card."
        ));
    }
}
