package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayTargetCardFromGraveyardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayTargetCardFromGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayTargetCardFromGraveyardWithoutPayingManaCostEffect) effect;
        UUID controllerId = entry.getControllerId();

        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return;
        }

        // Must still be in the controller's own graveyard and match the filter.
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null || !graveyardOwnerId.equals(controllerId)
                || !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (illegal target)."));
            return;
        }

        // "You may play" — offer the free play as a may choice.
        String prompt = entry.getCard().getName() + " — Play " + targetCard.getName()
                + " from your graveyard without paying its mana cost?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard,
                controllerId,
                List.of(e),
                prompt
        ));
    }
}
