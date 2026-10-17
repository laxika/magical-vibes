package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Handles a replacement effect that changes how a permanent enters the battlefield. */
public interface EntryReplacementHandlerBean {

    Class<? extends CardEffect> handledEffect();

    default void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        throw new UnsupportedOperationException("Entry replacement does not implement apply()");
    }

    /**
     * Applies the replacement with the announced X value of the spell that is putting the
     * permanent onto the battlefield. Existing entry replacements do not need this context.
     */
    default void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                       CardEffect effect, int xValue) {
        apply(gameData, controllerId, enteringPermanent, effect);
    }

    /**
     * Applies the replacement knowing whether the entering permanent's spell was kicked. Entry
     * replacements that do not depend on kicker fall back to the X-value form.
     */
    default void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                       CardEffect effect, int xValue, boolean kicked) {
        apply(gameData, controllerId, enteringPermanent, effect, xValue);
    }
}
