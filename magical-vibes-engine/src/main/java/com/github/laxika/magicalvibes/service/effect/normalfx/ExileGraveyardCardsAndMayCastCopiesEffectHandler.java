package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves The Tale of Tamiyo's graveyard exile and copy offers. */
@Component
@RequiredArgsConstructor
public class ExileGraveyardCardsAndMayCastCopiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardCardsAndMayCastCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileGraveyardCardsAndMayCastCopiesEffect copyEffect =
                (ExileGraveyardCardsAndMayCastCopiesEffect) effect;
        List<UUID> targetCardIds = entry.targetsForEffect(effect);
        if (targetCardIds.isEmpty()) {
            targetCardIds = entry.getTargetCardIds();
        }

        List<Card> copies = new ArrayList<>();
        ExileTargetCardFromGraveyardAndMayCastCopyEffect mayCastEffect =
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        copyEffect.filter(), copyEffect.scope(), 0, false, false);
        List<UUID> validGraveyardOwners = copyEffect.scope()
                .graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId());
        for (UUID targetCardId : targetCardIds) {
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
            if (targetCard == null || graveyardOwnerId == null
                    || !validGraveyardOwners.contains(graveyardOwnerId)
                    || copyEffect.filter() != null
                    && !predicateEvaluationService.matchesCardPredicate(
                    targetCard, copyEffect.filter(), entry.getCard().getId(), gameData,
                    graveyardOwnerId, entry.getSourcePermanentId(),
                    entry.getTriggeringPermanentPowerAtTrigger())) {
                continue;
            }

            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCardId);
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
            gameLogService.append(gameData, GameLog.isExiled(targetCard));

            Card copy = copySupport.createCopyCard(targetCard);
            exileService.exileCard(gameData, entry.getControllerId(), copy);
            copies.add(copy);
        }

        for (int i = copies.size() - 1; i >= 0; i--) {
            Card copy = copies.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    copy,
                    entry.getControllerId(),
                    List.of(mayCastEffect),
                    "Cast the copy of " + copy.getName() + "?",
                    copy.getId()));
        }
    }
}
