package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "41")
public class EtheriumAstrolabe extends Card {

    public EtheriumAstrolabe() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificeArtifactCost(), new DrawCardEffect(1)),
                "{B}, {T}, Sacrifice an artifact: Draw a card."
        ));
    }
}
