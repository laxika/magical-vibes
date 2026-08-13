package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "299")
@CardRegistration(set = "ODY", collectorNumber = "306")
@CardRegistration(set = "TPR", collectorNumber = "229")
public class PatchworkGnomes extends Card {

    public PatchworkGnomes() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new RegenerateEffect()
                ),
                "Discard a card: Regenerate Patchwork Gnomes."
        ));
    }
}
