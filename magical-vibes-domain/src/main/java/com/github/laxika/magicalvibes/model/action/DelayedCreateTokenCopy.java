package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

import java.util.UUID;

/** Delayed trigger that creates a copy of a source card at the beginning of the next end step. */
public record DelayedCreateTokenCopy(
        UUID controllerId,
        CreateTokenCopyOfSourceEffect copyEffect,
        Card sourceCard
) implements DelayedAction {
}
