package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
public class AttachSourceAuraToChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceAuraToChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID chooserId = entry.getTargetId();
        if (aura == null || chooserId == null) {
            return;
        }

        PermanentPredicate filter = ((AttachSourceAuraToChosenPermanentEffect) effect).filter();
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId == null) {
            return;
        }

        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)
                        && auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, permanent)) {
                    validTargetIds.add(permanent.getId());
                }
            }
        }

        if (validTargetIds.isEmpty()) {
            return;
        }
        if (validTargetIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.AttachSourceAuraToChosenPermanent(aura.getId()));
            playerInputService.beginPermanentChoice(gameData, chooserId, validTargetIds,
                    "Choose a permanent to attach " + aura.getCard().getName() + " to.");
            return;
        }

        attach(gameData, aura, gameQueryService.findPermanentById(gameData, validTargetIds.getFirst()));
    }

    private void attach(GameData gameData, Permanent aura, Permanent target) {
        if (target == null) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(target.getId());
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", target.getCard(), "."));
        triggerCollectionService.checkAuraAttachedTriggers(gameData, aura, target.getId());
        log.info("Game {} - {} attached to {}", gameData.id, aura.getCard().getName(), target.getCard().getName());
    }
}
