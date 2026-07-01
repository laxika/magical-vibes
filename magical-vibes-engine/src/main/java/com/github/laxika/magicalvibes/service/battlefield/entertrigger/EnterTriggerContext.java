package com.github.laxika.magicalvibes.service.battlefield.entertrigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Per-source-permanent context for a single enter-the-battlefield trigger scan.
 * <p>
 * A scan method (see {@code EnterTriggerScanService}) builds one context per triggering
 * permanent and hands each of that permanent's trigger effects to
 * {@link EnterTriggerHandlerRegistry#dispatch}. The context encapsulates every difference
 * between the various scans (who controls the triggered ability, the default target player,
 * how many copies to enqueue, how "may" abilities are queued) so that the individual
 * {@link EnterTriggerHandler}s stay scan-agnostic.
 */
@Getter
@Builder
public class EnterTriggerContext {

    private final GameData gameData;
    /** The permanent whose ability is triggering. */
    private final Permanent sourcePermanent;
    /** The player who controls (and makes decisions for) the triggered ability. */
    private final UUID abilityControllerId;
    /** The card that entered the battlefield and caused the scan. */
    private final Card enteringCard;
    /**
     * The player recorded as the triggered ability's target on the stack entry
     * (e.g. the opponent whose creature entered, or the controller of an entering land).
     * {@code null} for triggers that need no player target — note that some scans (e.g.
     * "any other creature enters") deliberately leave this {@code null} so that generic
     * and self-referential effects are not handed a stray player target.
     */
    private final UUID defaultTargetPlayerId;
    /** The controller of the permanent that entered and caused this scan. */
    private final UUID enteringControllerId;
    /**
     * The card id preserved on a {@code MayPayManaEffect} stack entry so the wrapped effect
     * can reference it at resolution time (e.g. Mirrorworks' entering permanent). {@code null}
     * when no such reference is needed.
     */
    private final UUID mayPayTargetCardId;
    /** How many copies of each triggered ability to enqueue (e.g. Naban doubling). */
    private final int perEffectTriggerCount;
    /**
     * When {@code true}, the registry's default fallback skips effects that choose a target
     * (used by the "any other creature enters" scan, which only auto-queues non-targeting
     * triggers plus the explicitly handled life-gain / damage triggers).
     */
    private final boolean defaultSkipsTargetingEffects;

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final EnterTriggerHandlerRegistry registry;

    public Card sourceCard() {
        return sourcePermanent.getCard();
    }

    /** Puts {@code effect} onto the stack as a triggered ability, honouring the trigger count. */
    public void enqueue(CardEffect effect) {
        enqueue(effect, defaultTargetPlayerId);
    }

    /**
     * Puts {@code effect} onto the stack targeting {@code targetPlayerId}, honouring the trigger
     * count. Used by handlers (e.g. damage-to-a-player) that need a target other than the scan's
     * default.
     */
    public void enqueue(CardEffect effect, UUID targetPlayerId) {
        for (int i = 0; i < perEffectTriggerCount; i++) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard(),
                    abilityControllerId,
                    sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    targetPlayerId,
                    sourcePermanent.getId()
            ));
        }
    }

    /** Queues a "you may" triggered ability, preserving target/source context when present. */
    public void queueMay(MayEffect may) {
        if (defaultTargetPlayerId != null) {
            gameData.queueMayAbility(sourceCard(), abilityControllerId, may, defaultTargetPlayerId, sourcePermanent.getId());
        } else {
            gameData.queueMayAbility(sourceCard(), abilityControllerId, may);
        }
    }

    /** Queues a "you may pay" triggered ability, preserving the referenced card id when present. */
    public void queueMayPay(MayPayManaEffect mayPay) {
        gameData.queueMayAbility(sourceCard(), abilityControllerId, mayPay, mayPayTargetCardId);
    }

    /** Re-enters the registry for a wrapped effect (used by conditional handlers). */
    public void dispatch(CardEffect effect) {
        registry.dispatch(this, effect);
    }

    public void log(String message) {
        gameBroadcastService.logAndBroadcast(gameData, message);
    }

    public void logTriggered() {
        log(sourceCard().getName() + "'s ability triggers.");
    }
}
