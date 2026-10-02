package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedAurasToLegalTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Attaches Auras selected by a library-to-battlefield effect before the resolution ends. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSelectedAurasToLegalTargetsEffectHandler implements NormalEffectHandlerBean {
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSelectedAurasToLegalTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        continueAttaching(gameData, entry,
                ((AttachSelectedAurasToLegalTargetsEffect) effect).auraPermanentIds());
    }

    public void completeChoice(GameData gameData, UUID targetId,
                               PermanentChoiceContext.AttachSelectedAuraToLegalTarget context) {
        Permanent aura = gameQueryService.findPermanentById(gameData, context.auraPermanentId());
        if (aura != null && isLegalTarget(gameData, aura, targetId)) {
            attach(gameData, aura, targetId);
        }

        if (!context.remainingAuraPermanentIds().isEmpty()
                && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex,
                    List.of(new AttachSelectedAurasToLegalTargetsEffect(
                            context.remainingAuraPermanentIds())));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void continueAttaching(GameData gameData, StackEntry entry, List<UUID> auraIds) {
        for (int i = 0; i < auraIds.size(); i++) {
            Permanent aura = gameQueryService.findPermanentById(gameData, auraIds.get(i));
            if (aura == null || !aura.getCard().isAura() || aura.isAttached()
                    || aura.getCard().isEnchantZone()) {
                continue;
            }

            UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
            if (auraControllerId == null) {
                continue;
            }

            List<UUID> permanentIds = new ArrayList<>();
            gameData.forEachPermanent((ignored, permanent) -> {
                if (!gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                        && auraAttachmentService.canEnchant(
                        gameData, aura.getCard(), auraControllerId, permanent)) {
                    permanentIds.add(permanent.getId());
                }
            });
            List<UUID> playerIds = aura.getCard().isEnchantPlayer()
                    ? gameData.orderedPlayerIds.stream()
                    .filter(playerId -> auraAttachmentService.canEnchantPlayer(
                            gameData, aura.getCard(), auraControllerId, playerId))
                    .toList()
                    : List.of();

            if (permanentIds.isEmpty() && playerIds.isEmpty()) {
                continue;
            }
            List<UUID> remaining = auraIds.subList(i + 1, auraIds.size());
            if (permanentIds.size() + playerIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AttachSelectedAuraToLegalTarget(
                                aura.getId(), remaining));
                playerInputService.beginAnyTargetChoice(gameData, auraControllerId,
                        permanentIds, playerIds,
                        "Choose a permanent or player for " + aura.getCard().getName() + " to enchant.");
                return;
            }

            attach(gameData, aura,
                    permanentIds.isEmpty() ? playerIds.getFirst() : permanentIds.getFirst());
        }
    }

    private boolean isLegalTarget(GameData gameData, Permanent aura, UUID targetId) {
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId == null) {
            return false;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        return target != null
                ? !gameQueryService.cantBeEnchantedByOtherAuras(gameData, target)
                && auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, target)
                : gameData.playerIds.contains(targetId)
                && aura.getCard().isEnchantPlayer()
                && auraAttachmentService.canEnchantPlayer(
                gameData, aura.getCard(), auraControllerId, targetId);
    }

    private void attach(GameData gameData, Permanent aura, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(targetId);
        aura.setTimestamp(gameData.nextTimestamp());
        if (target != null) {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(aura.getCard(), " is now attached to ", target.getCard(), "."));
            triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, targetId);
            log.info("Game {} - {} attached to {}", gameData.id,
                    aura.getCard().getName(), target.getCard().getName());
        } else {
            gameLogService.append(gameData, GameLog.cardThen(aura.getCard(),
                    " is now attached to " + gameData.playerIdToName.get(targetId) + "."));
        }
    }
}
