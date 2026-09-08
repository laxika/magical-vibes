package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesPermanentToSacrificeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a non-targeting choice where the controller chooses the opponent who chooses a
 * matching permanent controlled by the ability's controller to sacrifice.
 */
@Component
@RequiredArgsConstructor
public class OpponentChoosesPermanentToSacrificeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesPermanentToSacrificeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (OpponentChoosesPermanentToSacrificeEffect) effect;
        UUID sacrificingPlayerId = entry.getControllerId();
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(sacrificingPlayerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        if (opponents.size() == 1) {
            beginPermanentChoice(gameData, opponents.getFirst(), sacrificingPlayerId,
                    entry.getCard().getName(), e.filter());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentForPermanentSacrifice(
                        sacrificingPlayerId, entry.getCard().getName(), e.filter()));
        playerInputService.beginPlayerChoice(gameData, sacrificingPlayerId, opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.ChooseOpponentForPermanentSacrifice context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.sacrificingPlayerId())) {
            return;
        }
        beginPermanentChoice(gameData, chosenOpponentId, context.sacrificingPlayerId(),
                context.sourceCardName(), context.filter());
    }

    public void completePermanentChoice(GameData gameData, UUID permanentId,
                                        PermanentChoiceContext.OpponentChoosesPermanentToSacrifice context) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null
                && context.sacrificingPlayerId().equals(
                        gameQueryService.findPermanentController(gameData, permanentId))
                && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, context.filter())) {
            destructionSupport.sacrificeAndLog(gameData, permanent, context.sacrificingPlayerId());
        }
    }

    private void beginPermanentChoice(GameData gameData, UUID choosingPlayerId,
                                      UUID sacrificingPlayerId, String sourceCardName,
                                      com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield == null) {
            return;
        }
        List<UUID> matchingIds = battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }
        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                destructionSupport.sacrificeAndLog(gameData, permanent, sacrificingPlayerId);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.OpponentChoosesPermanentToSacrifice(
                        choosingPlayerId, sacrificingPlayerId, sourceCardName, filter));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, matchingIds,
                sourceCardName + " — choose a permanent to sacrifice.");
    }
}
