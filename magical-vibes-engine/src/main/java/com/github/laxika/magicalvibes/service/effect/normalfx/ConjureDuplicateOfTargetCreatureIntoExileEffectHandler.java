package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureIntoExileEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetCreatureIntoExileEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetCreatureIntoExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForBoundEffectGroup(effect);
        if (targetIds == null) {
            targetIds = !entry.getTargetIds().isEmpty()
                    ? entry.getTargetIds()
                    : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty()
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        if (target == null || target.getCard().isToken() || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        // A duplicate of a face-down creature has its face-down values, not its hidden card (CR 707.2, 708.2a).
        Card duplicate = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                PermanentCopierService.cardToCopy(target), new CreateTokenCopyOfTargetPermanentEffect());
        duplicate.setToken(false);
        duplicate.setConjured(true);
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addToExile(entry.getControllerId(), duplicate, entry.getSourcePermanentId());
        gameLogService.append(gameData, target.isFaceDown()
                ? GameLog.text(FaceDownVisibility.sourceName(gameData, entry) + " conjures a duplicate of "
                + FaceDownVisibility.publicName(target) + " into exile.")
                : GameLog.textCardText(FaceDownVisibility.sourceName(gameData, entry) + " conjures a duplicate of ",
                target.getCard(), " into exile."));
        ActionEvents.conjured(gameData, entry.getControllerId(), 1);
    }
}
