package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Rejoinder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Elite Interceptor // Rejoinder (SOS 12).
 *
 * <p>The front face enters prepared, allowing its stored Rejoinder spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "12")
public class EliteInterceptorRejoinder extends Card {

    public EliteInterceptorRejoinder() {
        setBackFaceCard(new Rejoinder());

        // This creature enters prepared.
        addStaticEffect(new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Rejoinder";
    }
}
