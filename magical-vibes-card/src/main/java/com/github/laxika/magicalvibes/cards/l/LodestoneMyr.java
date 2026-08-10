package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "200")
public class LodestoneMyr extends Card {

    public LodestoneMyr() {
        // Tap an untapped artifact you control: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsArtifactPredicate()),
                        new BoostSelfEffect(1, 1)),
                "Tap an untapped artifact you control: This creature gets +1/+1 until end of turn."));
    }
}
