package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GravestormEffect;

@CardRegistration(set = "MKC", collectorNumber = "23")
@CardRegistration(set = "MKC", collectorNumber = "333")
public class FollowTheBodies extends Card {

    public FollowTheBodies() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.ON_SELF_CAST, new GravestormEffect());
    }
}
