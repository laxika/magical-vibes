package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "NEO", collectorNumber = "51")
public class DisruptionProtocol extends Card {

    public DisruptionProtocol() {
        addEffect(EffectSlot.SPELL, new TapPermanentOrPayManaCost(
                "{1}", new PermanentIsArtifactPredicate(), "an untapped artifact"));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
