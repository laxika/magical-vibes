package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Carries the common data for an effect stored on an emblem during a spell-cast event. */
public record EmblemTriggerMatchContext(
        GameData gameData,
        Emblem emblem,
        UUID controllerId,
        Card spellCard,
        CardEffect rawEffect
) {}
