package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "NEO", collectorNumber = "51")
public class DisruptionProtocol extends Card {

    public DisruptionProtocol() {
        addCastingCost(AnyOfCost.of(
                new TapMultiplePermanentsCost(1, new PermanentIsArtifactPredicate()), new PayManaCost("{1}")));
        addSpellEffect(new CounterSpellEffect());
    }
}
