package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.ForumFavor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Honorbound Page // Forum's Favor (SOS 19).
 *
 * <p>The front face enters prepared, allowing its stored Forum's Favor spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "19")
public class HonorboundPageForumsFavor extends Card {

    public HonorboundPageForumsFavor() {
        setBackFaceCard(new ForumFavor());

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ForumFavor";
    }
}
