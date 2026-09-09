package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TwofoldIntent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Quill-Blade Laureate // Twofold Intent (SOS 27).
 * <p>
 * Front face — 1/1 Human Cleric with Double strike (auto-loaded from Scryfall) that enters prepared.
 */
@CardRegistration(set = "SOS", collectorNumber = "27")
public class QuillBladeLaureateTwofoldIntent extends Card {

    public QuillBladeLaureateTwofoldIntent() {
        setBackFaceCard(new TwofoldIntent());

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TwofoldIntent";
    }
}
