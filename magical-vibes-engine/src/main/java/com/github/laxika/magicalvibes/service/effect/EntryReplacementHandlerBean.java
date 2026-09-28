package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Handles a replacement effect that changes how a permanent enters the battlefield. */
public interface EntryReplacementHandlerBean {

    Class<? extends CardEffect> handledEffect();

    default void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        apply(gameData, controllerId, enteringPermanent, effect, 0);
    }

    /**
     * Applies the replacement with the announced X value of the spell that is putting the
     * permanent onto the battlefield. Existing entry replacements do not need this context.
     */
    default void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                       CardEffect effect, int xValue) {
        throw new UnsupportedOperationException("Entry replacement does not implement apply()");
    }
}
