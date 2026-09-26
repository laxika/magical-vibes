package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Redoubled Stormsinger's temporary token-copy attack trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachTokenEnteredThisTurnEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentEnteredBattlefieldThisTurnPredicate ENTERED_THIS_TURN =
            new PermanentEnteredBattlefieldThisTurnPredicate();

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachTokenEnteredThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> matchingTokens = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, ENTERED_THIS_TURN))
                .toList();
        if (matchingTokens.isEmpty()) {
            return;
        }

        int tokenCount = matchingTokens.stream()
                .mapToInt(permanent -> gameQueryService.getTokenCreationAmount(
                        gameData, controllerId, 1, permanent.getCard().getSubtypes(), true))
                .sum();
        if (tokenCount <= 0) {
            return;
        }

        beginAttackTargetChoice(gameData, new PermanentChoiceContext.CreateTokenCopiesOfEnteredThisTurnAttacking(
                controllerId, entry.getCard(), entry.getSourcePermanentId(),
                matchingTokens.stream().map(Permanent::getId).toList(), tokenCount, List.of()));
    }

    private void beginAttackTargetChoice(
            GameData gameData, PermanentChoiceContext.CreateTokenCopiesOfEnteredThisTurnAttacking context) {
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(context.controllerId()))
                .toList();
        List<UUID> planeswalkerIds = opponentIds.stream()
                .flatMap(opponentId -> gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream())
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginAnyTargetChoice(
                gameData, context.controllerId(), planeswalkerIds, opponentIds,
                "Choose the player or planeswalker for the next token to attack.");
    }
}
