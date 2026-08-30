package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CastTargetInstantOrSorceryFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastTargetInstantOrSorceryFromGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();

        List<UUID> boundTargets = entry.targetsForEffect(effect);
        List<UUID> targetCardIds = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds()
                : !boundTargets.isEmpty()
                ? boundTargets
                : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        if (targetCardIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        for (int i = targetCardIds.size() - 1; i >= 0; i--) {
            UUID targetCardId = targetCardIds.get(i);
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target no longer in graveyard)."));
                continue;
            }

            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
            if (graveyardOwnerId == null) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target not in any graveyard)."));
                continue;
            }
            boolean validScope = switch (e.scope()) {
                case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(controllerId);
                case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(controllerId);
                case ALL_GRAVEYARDS -> true;
            };
            if (!validScope) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target not in a valid graveyard)."));
                continue;
            }

            boolean instantOrSorcery = targetCard.hasType(CardType.INSTANT) || targetCard.hasType(CardType.SORCERY);
            boolean matchesFilter = e.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(
                    targetCard,
                    e.filter(),
                    entry.getCard().getId(),
                    gameData,
                    graveyardOwnerId,
                    entry.getSourcePermanentId(),
                    entry.getTriggeringPermanentPowerAtTrigger(),
                    entry.getXValue());
            if (!instantOrSorcery || !matchesFilter) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target is not an instant or sorcery)."));
                continue;
            }

            String prompt = e.withoutPayingManaCost()
                    ? entry.getCard().getName() + " — Cast " + targetCard.getName()
                    + " without paying its mana cost?"
                    : entry.getCard().getName() + " — Cast " + targetCard.getName() + "?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    targetCard, controllerId, List.of(e), prompt,
                    entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(), entry.getXValue()));
        }
    }
}
