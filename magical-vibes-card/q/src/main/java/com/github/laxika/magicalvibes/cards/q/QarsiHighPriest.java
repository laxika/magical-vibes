package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "80")
public class QarsiHighPriest extends Card {

    public QarsiHighPriest() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ManifestTopCardEffect()
                ),
                "{1}{B}, {T}, Sacrifice another creature: Manifest the top card of your library."
        ));
    }
}
