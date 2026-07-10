package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * CR 613.2/613.7 layer-2 control semantics. Every control-changing effect is a floating
 * {@code L2_CONTROL} continuous effect on {@link GameData#floatingEffects}; the controller of a
 * permanent is DERIVED — the newest active control effect wins, with none active the permanent
 * reverts to its default controller ({@link GameData#defaultControllerOf}). Because the rest of
 * the engine equates battlefield-list membership with control, {@link #recomputeControl}
 * physically moves the permanent between {@code playerBattlefields} lists whenever the derived
 * controller changes. Moves keep the permanent's CR 613.7 timestamp (a control change is not a
 * zone change — no ETB triggers fire) and make it summoning sick for the new controller
 * (CR 302.6; effects like Threaten grant haste separately).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatureControlService {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    /**
     * Creates a floating control effect giving {@code newControllerId} control of {@code target}
     * and immediately recomputes the target's controller.
     *
     * @param gameData          the current game state
     * @param newControllerId   the player the effect gives control to
     * @param target            the permanent changing control
     * @param wrappedEffect     the control effect being applied (drives layer classification)
     * @param duration          how long the effect lasts
     * @param sourcePermanentId the source permanent for source/attachment-scoped durations, else {@code null}
     * @param sourceCardName    name of the card whose spell/ability created the effect
     */
    public void applyControlEffect(GameData gameData, UUID newControllerId, Permanent target,
                                   CardEffect wrappedEffect, EffectDuration duration,
                                   UUID sourcePermanentId, String sourceCardName) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceCardName, sourcePermanentId, newControllerId,
                wrappedEffect, target.getId(), null, null, duration, 0));
        recomputeControl(gameData, target);
    }

    /**
     * Recomputes who controls the permanent from the active control effects and physically
     * moves it between battlefield lists if the derived controller differs from the current
     * one. Maintains the {@link GameData#stolenCreatures} ownership record (first move away
     * from the owner records them; arriving back at the owner clears the record).
     *
     * <p>No-ops if the permanent is not on any battlefield or control is unchanged.
     */
    public void recomputeControl(GameData gameData, Permanent permanent) {
        UUID current = gameData.findControllerOf(permanent.getId());
        if (current == null) {
            return;
        }
        UUID derived = gameData.deriveControllerOf(permanent.getId());
        if (derived == null || derived.equals(current)) {
            return;
        }
        boolean revertedToDefault = gameData.newestControlEffectFor(permanent.getId()) == null;

        gameData.playerBattlefields.get(current).remove(permanent);
        gameData.playerBattlefields.get(derived).add(permanent);
        permanent.setSummoningSick(true);

        gameData.stolenCreatures.putIfAbsent(permanent.getId(), current);
        if (derived.equals(gameData.stolenCreatures.get(permanent.getId()))) {
            gameData.stolenCreatures.remove(permanent.getId());
        }

        // Legacy behavior preserved: an attached Equipment reverting to its default controller
        // becomes unattached.
        if (revertedToDefault && permanent.isAttached()
                && permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            permanent.setAttachedTo(null);
            gameData.expireFloatingEffectsForUnattachedSource(permanent.getId());
            String unattachLog = permanent.getCard().getName() + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached on control change", gameData.id, permanent.getCard().getName());
        }

        String newControllerName = gameData.playerIdToName.get(derived);
        String logEntry = revertedToDefault
                ? permanent.getCard().getName() + " returns to " + newControllerName + "'s control."
                : newControllerName + " gains control of " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} controls {}", gameData.id, newControllerName, permanent.getCard().getName());

        // "For as long as you control [source]" effects keyed to THIS permanent end when it
        // changes controllers away from their creator; cascade to the permanents they held.
        expireSourceControllerDependentEffects(gameData, permanent.getId());
    }

    /**
     * Full control reconciliation: ensures every attached control Aura (In Bolas's Clutches,
     * Persuasion, ...) is backed by a {@code WHILE_ATTACHED} floating effect, expires effects
     * whose condition no longer holds, and recomputes control of every permanent touched by a
     * control effect or ownership record. Called after battlefield removals
     * ({@code AuraAttachmentService.removeOrphanedAuras}) and at the cleanup step, replacing
     * the old {@code returnStolenCreatures} revert-to-owner scan — control falls back to the
     * NEXT most recent still-active effect, not blindly to the owner.
     */
    public void reconcileControl(GameData gameData) {
        ensureControlAuraEffects(gameData);
        expireStaleControlEffects(gameData);

        Set<UUID> affectedIds = new LinkedHashSet<>(gameData.stolenCreatures.keySet());
        for (FloatingContinuousEffect fe : List.copyOf(gameData.floatingEffects)) {
            if (fe.isControlEffect() && fe.affectedPermanentId() != null) {
                affectedIds.add(fe.affectedPermanentId());
            }
        }
        for (UUID permanentId : affectedIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                gameData.stolenCreatures.remove(permanentId);
                gameData.expireControlEffectsForDepartedPermanent(permanentId);
                continue;
            }
            recomputeControl(gameData, permanent);
        }
    }

    /**
     * Every Aura with a {@link ControlEnchantedCreatureEffect} static ability that is attached
     * to a permanent must have a matching {@code WHILE_ATTACHED} floating control effect. The
     * resolution sites that attach control Auras create it directly (so the steal is immediate);
     * this safety net covers reattachment paths (Aura Graft, ...) where the old attachment's
     * effect was expired and a new one is due at the new attach timestamp.
     */
    private void ensureControlAuraEffects(GameData gameData) {
        List<Permanent> controlAuras = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isAttached()
                    && p.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect)) {
                controlAuras.add(p);
            }
        });
        for (Permanent aura : controlAuras) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted == null) continue;
            boolean present = gameData.controlEffectsFor(enchanted.getId()).stream()
                    .anyMatch(fe -> fe.duration() == EffectDuration.WHILE_ATTACHED
                            && aura.getId().equals(fe.sourcePermanentId()));
            if (!present) {
                UUID auraController = gameData.findControllerOf(aura.getId());
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), aura.getCard().getName(), aura.getId(), auraController,
                        new ControlEnchantedCreatureEffect(), enchanted.getId(), null, null,
                        EffectDuration.WHILE_ATTACHED, 0));
            }
        }
    }

    /**
     * Expires control effects whose "for as long as ..." condition stopped holding (such
     * effects end for good — they do not resume, CR 611.2b):
     * <ul>
     *   <li>{@code WHILE_ATTACHED} — the source Aura left or enchants something else (the
     *       departure/unattach hooks normally expire these already; this covers moves that
     *       bypass them).</li>
     *   <li>{@code WHILE_SOURCE_ON_BATTLEFIELD} — the source left, or its creator no longer
     *       controls it (Olivia Voldaren's "for as long as you control Olivia Voldaren").</li>
     *   <li>{@link GainControlOfEnchantedTargetEffect} — the affected permanent is no longer
     *       enchanted (Rootwater Matriarch).</li>
     * </ul>
     */
    private void expireStaleControlEffects(GameData gameData) {
        for (FloatingContinuousEffect fe : List.copyOf(gameData.floatingEffects)) {
            if (!fe.isControlEffect()) continue;
            boolean stale = false;
            if (fe.duration() == EffectDuration.WHILE_ATTACHED) {
                Permanent source = fe.sourcePermanentId() == null ? null
                        : gameQueryService.findPermanentById(gameData, fe.sourcePermanentId());
                stale = source == null || !source.isAttached()
                        || !source.getAttachedTo().equals(fe.affectedPermanentId());
            } else if (fe.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD) {
                UUID sourceController = fe.sourcePermanentId() == null ? null
                        : gameData.findControllerOf(fe.sourcePermanentId());
                stale = sourceController == null || !sourceController.equals(fe.controllerId());
            }
            if (!stale && fe.effect() instanceof GainControlOfEnchantedTargetEffect) {
                Permanent affected = gameQueryService.findPermanentById(gameData, fe.affectedPermanentId());
                stale = affected == null || !gameQueryService.isEnchanted(gameData, affected);
            }
            if (stale) {
                gameData.floatingEffects.remove(fe);
            }
        }
    }

    /**
     * When a permanent changes controllers, "for as long as you control [source]" control
     * effects keyed to it as their SOURCE end if their creator lost it; the permanents they
     * were holding get recomputed.
     */
    private void expireSourceControllerDependentEffects(GameData gameData, UUID sourcePermanentId) {
        UUID sourceController = gameData.findControllerOf(sourcePermanentId);
        List<FloatingContinuousEffect> expired = new ArrayList<>();
        for (FloatingContinuousEffect fe : List.copyOf(gameData.floatingEffects)) {
            if (fe.isControlEffect()
                    && fe.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                    && sourcePermanentId.equals(fe.sourcePermanentId())
                    && !fe.controllerId().equals(sourceController)) {
                gameData.floatingEffects.remove(fe);
                expired.add(fe);
            }
        }
        for (FloatingContinuousEffect fe : expired) {
            Permanent affected = gameQueryService.findPermanentById(gameData, fe.affectedPermanentId());
            if (affected != null) {
                recomputeControl(gameData, affected);
            }
        }
    }
}
