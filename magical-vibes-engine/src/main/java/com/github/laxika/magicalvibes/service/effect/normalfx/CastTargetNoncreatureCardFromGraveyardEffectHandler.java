package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetNoncreatureCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CastTargetNoncreatureCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetNoncreatureCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastTargetNoncreatureCardFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();

        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target no longer in graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        boolean matchesFilter = predicateEvaluationService.matchesCardPredicate(
                targetCard,
                e.cardFilter(),
                entry.getCard().getId(),
                gameData,
                graveyardOwnerId,
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue());
        if (graveyardOwnerId == null || !graveyardOwnerId.equals(controllerId) || !matchesFilter) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (illegal target)."));
            return;
        }

        if (targetCard.hasType(CardType.LAND)) {
            gameLogService.append(gameData, GameLog.cardThen(targetCard, " can't be cast from the graveyard."));
            return;
        }

        String prompt = entry.getCard().getName() + " — Cast " + targetCard.getName()
                + " from your graveyard without paying its mana cost?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard,
                controllerId,
                List.of(e),
                prompt,
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue()));
    }
}
