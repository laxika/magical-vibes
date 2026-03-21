package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "64")
public class SageOfLatNam extends Card {

    public SageOfLatNam() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeArtifactCost(), new DrawCardEffect()),
                "{T}, Sacrifice an artifact: Draw a card."
        ));
    }
}
