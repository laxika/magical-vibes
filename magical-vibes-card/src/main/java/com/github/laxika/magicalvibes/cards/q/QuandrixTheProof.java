package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "SOS", collectorNumber = "218")
public class QuandrixTheProof extends Card {

    public QuandrixTheProof() {
        // Cascade: when this spell is cast, the controller may cast the first qualifying card from
        // the top of their library without paying its mana cost.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());

        // Instant and sorcery spells cast from its controller's hand have cascade.
        addEffect(EffectSlot.GRANT_CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND, new CascadeEffect());
    }
}
