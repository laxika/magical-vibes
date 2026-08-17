package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

/** Back face of Uninvited Geist. */
public class UnimpededTrespasser extends Card {

    public UnimpededTrespasser() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
