package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPermanentId = entry.getTargetId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetPermanentId == null || targetCardIds.size() != 1) {
            return;
        }

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetPermanentId);
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardIds.getFirst());
        if (targetPermanent == null || targetCard == null
                || !gameQueryService.isArtifact(targetPermanent)
                || !targetCard.hasType(CardType.ARTIFACT)) {
            return;
        }

        UUID artifactControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (artifactControllerId == null || !artifactControllerId.equals(graveyardOwnerId)) {
            return;
        }

        if (!permanentRemovalService.removePermanentToGraveyard(gameData, targetPermanent)) {
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, artifactControllerId, targetPermanent.getCard());
        gameLogService.append(gameData, GameLog.cardThen(targetPermanent.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        returnHandler.resolveForController(gameData, entry,
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        1, false, false),
                graveyardOwnerId);
    }
}
