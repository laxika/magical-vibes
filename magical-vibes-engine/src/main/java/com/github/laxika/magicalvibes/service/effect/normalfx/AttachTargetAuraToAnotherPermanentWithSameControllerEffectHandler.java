package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentWithSameControllerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Simic Guildmage's Aura-attachment ability. The targeted Aura stays targeted, while the
 * ability's controller chooses a legal destination during resolution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetAuraToAnotherPermanentWithSameControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final CreatureControlService creatureControlService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraToAnotherPermanentWithSameControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (aura == null) {
            return;
        }

        Permanent currentHost = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        UUID hostControllerId = currentHost == null
                ? null
                : gameQueryService.findPermanentController(gameData, currentHost.getId());
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (currentHost == null || hostControllerId == null || auraControllerId == null
                || !aura.getCard().isAura()) {
            return;
        }

        List<UUID> validTargetIds = findValidTargetIds(gameData, aura, currentHost, hostControllerId,
                auraControllerId);
        if (validTargetIds.isEmpty()) {
            return;
        }
        if (validTargetIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachTargetAuraToAnotherPermanentWithSameController(aura.getId()));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargetIds,
                    "Choose another permanent to attach " + aura.getCard().getName() + " to.");
            return;
        }

        attach(gameData, aura, gameQueryService.findPermanentById(gameData, validTargetIds.getFirst()));
    }

    public void attachChosen(GameData gameData, UUID permanentId,
                             PermanentChoiceContext.AttachTargetAuraToAnotherPermanentWithSameController context) {
        Permanent aura = gameQueryService.findPermanentById(gameData, context.auraPermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        Permanent currentHost = aura == null
                ? null
                : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        UUID hostControllerId = currentHost == null
                ? null
                : gameQueryService.findPermanentController(gameData, currentHost.getId());
        UUID auraControllerId = aura == null
                ? null
                : gameQueryService.findPermanentController(gameData, aura.getId());

        if (aura == null || target == null || currentHost == null || hostControllerId == null
                || auraControllerId == null || target.getId().equals(currentHost.getId())
                || target.getId().equals(aura.getId()) || !aura.getCard().isAura()
                || !hostControllerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))
                || !auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, target)
                || gameQueryService.hasProtectionFromSource(gameData, target, aura)) {
            return;
        }

        attach(gameData, aura, target);
    }

    private List<UUID> findValidTargetIds(GameData gameData, Permanent aura, Permanent currentHost,
                                          UUID hostControllerId, UUID auraControllerId) {
        List<UUID> validTargetIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getId().equals(aura.getId()) || permanent.getId().equals(currentHost.getId())) {
                return;
            }
            if (!hostControllerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()))) {
                return;
            }
            if (auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, permanent)
                    && !gameQueryService.hasProtectionFromSource(gameData, permanent, aura)) {
                validTargetIds.add(permanent.getId());
            }
        });
        return validTargetIds;
    }

    private void attach(GameData gameData, Permanent aura, Permanent target) {
        Permanent oldHost = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(target.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", target.getCard(), "."));
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, target.getId());
        if (oldHost != null) {
            creatureControlService.recomputeControl(gameData, oldHost);
        }
        creatureControlService.recomputeControl(gameData, target);
        log.info("Game {} - {} attached to {}", gameData.id, aura.getCard().getName(), target.getCard().getName());
    }
}
