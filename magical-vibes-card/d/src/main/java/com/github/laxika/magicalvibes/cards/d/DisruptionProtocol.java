package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "NEO", collectorNumber = "51")
public class DisruptionProtocol extends Card {

    public DisruptionProtocol() {
        addCastingCost(new TapPermanentOrPayManaCost(
                "{1}", new PermanentIsArtifactPredicate(), "an untapped artifact"));
        addSpellEffect(new CounterSpellEffect());
    }
}
