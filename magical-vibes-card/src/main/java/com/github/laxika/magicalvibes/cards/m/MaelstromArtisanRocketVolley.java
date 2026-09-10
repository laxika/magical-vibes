package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RocketVolley;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Maelstrom Artisan // Rocket Volley (SOS 122).
 *
 * <p>The creature enters prepared, allowing its Rocket Volley prepare spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "122")
public class MaelstromArtisanRocketVolley extends Card {

    public MaelstromArtisanRocketVolley() {
        setBackFaceCard(new RocketVolley());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RocketVolley";
    }
}
