package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Handles a replacement effect that changes how a permanent enters the battlefield. */
public interface EntryReplacementHandlerBean {

    Class<? extends CardEffect> handledEffect();

    void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect);
}
