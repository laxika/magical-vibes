package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffectHandler
        implements NormalEffectHandlerBean {

    private static final PermanentAnyOfPredicate CREATURE_OR_VEHICLE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesCreatureOrVehicleOrDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
        if (opponentId == null || !gameData.playerIds.contains(opponentId)) {
            return;
        }

        List<UUID> eligibleIds = destructionSupport.collectPermanentIds(gameData, opponentId,
                permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, CREATURE_OR_VEHICLE));
        if (!eligibleIds.isEmpty()
                && gameQueryService.canEffectCauseSacrifice(gameData, opponentId, entry.getControllerId())) {
            if (eligibleIds.size() == 1) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, eligibleIds.getFirst());
                if (permanent != null) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, opponentId);
                }
            } else {
                playerInputService.beginMultiPermanentChoice(gameData, opponentId, eligibleIds, 1,
                        new MultiPermanentChoiceContext.ForcedSacrifice(opponentId, List.of(), List.of()),
                        entry.getCard().getName() + " - Choose a creature or Vehicle to sacrifice.");
            }
            return;
        }

        List<?> hand = gameData.playerHands.get(opponentId);
        if (hand != null && !hand.isEmpty()) {
            gameData.discardCausedByOpponent = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, 1,
                    DiscardFollowUp.NONE);
        }
    }
}
