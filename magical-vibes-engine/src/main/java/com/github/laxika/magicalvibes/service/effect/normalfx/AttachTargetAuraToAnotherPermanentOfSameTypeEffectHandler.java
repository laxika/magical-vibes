package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetAuraToAnotherPermanentOfSameTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final CreatureControlService creatureControlService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraToAnotherPermanentOfSameTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (aura == null) {
            return;
        }

        Permanent currentHost = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (currentHost == null || !aura.getCard().isAura()
                || !isCreatureOrLand(gameData, currentHost)) {
            return;
        }

        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId == null) {
            return;
        }

        List<UUID> validTargetIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getId().equals(aura.getId()) || permanent.getId().equals(currentHost.getId())) {
                return;
            }
            if (sameType(gameData, currentHost, permanent)
                    && auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, permanent)
                    && !gameQueryService.hasProtectionFromSource(gameData, permanent, aura)) {
                validTargetIds.add(permanent.getId());
            }
        });

        if (validTargetIds.isEmpty()) {
            return;
        }
        if (validTargetIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachTargetAuraToAnotherPermanentOfSameType(aura.getId()));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargetIds,
                    "Choose another permanent to attach " + aura.getCard().getName() + " to.");
            return;
        }

        attach(gameData, aura, gameQueryService.findPermanentById(gameData, validTargetIds.getFirst()));
    }

    public void attachChosen(GameData gameData, UUID permanentId,
                             PermanentChoiceContext.AttachTargetAuraToAnotherPermanentOfSameType context) {
        Permanent aura = gameQueryService.findPermanentById(gameData, context.auraPermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        Permanent currentHost = aura == null
                ? null
                : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        UUID auraControllerId = aura == null
                ? null
                : gameQueryService.findPermanentController(gameData, aura.getId());

        if (aura == null || target == null || currentHost == null || auraControllerId == null
                || target.getId().equals(currentHost.getId()) || target.getId().equals(aura.getId())
                || !aura.getCard().isAura() || !isCreatureOrLand(gameData, currentHost)
                || !sameType(gameData, currentHost, target)
                || !auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, target)
                || gameQueryService.hasProtectionFromSource(gameData, target, aura)) {
            return;
        }

        attach(gameData, aura, target);
    }

    private boolean isCreatureOrLand(GameData gameData, Permanent permanent) {
        return gameQueryService.isCreature(gameData, permanent) || gameQueryService.isLand(gameData, permanent);
    }

    private boolean sameType(GameData gameData, Permanent currentHost, Permanent candidate) {
        return (gameQueryService.isCreature(gameData, currentHost) && gameQueryService.isCreature(gameData, candidate))
                || (gameQueryService.isLand(gameData, currentHost) && gameQueryService.isLand(gameData, candidate));
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
