package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;

    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect) {
        return TokenCopySupport.buildTokenCopyCard(sourceCard, effect);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfTargetPermanentEffect) effect;

        List<UUID> targetIds = entry.targetsForBoundEffectGroup(copyEffect);
        if (targetIds == null) {
            targetIds = !entry.getTargetIds().isEmpty()
                    ? entry.getTargetIds()
                    : !entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetCardIds()
                     : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty()
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, copyEffect.amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        if (copyCount <= 0) {
            return;
        }

        for (UUID targetId : targetIds) {
            resolveForTarget(gameData, entry, copyEffect, targetId, copyCount);
        }
    }

    void resolveForTarget(GameData gameData, StackEntry entry,
                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId) {
        resolveForTarget(gameData, entry, effect, targetId, 1);
    }

    void resolveForTarget(GameData gameData, StackEntry entry,
                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId, int copyCount) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPermanent == null) {
            return;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID tokenControllerId = entry.getControllerId();
        if (effect.createForTargetController()) {
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (targetControllerId != null) {
                tokenControllerId = targetControllerId;
            }
        }
        if (effect.chooseAttackTarget()) {
            int tokenCount = copyCount * gameQueryService.getTokenMultiplier(gameData, tokenControllerId);
            PermanentChoiceContext.CreateTokenCopiesAttacking context =
                    new PermanentChoiceContext.CreateTokenCopiesAttacking(
                            tokenControllerId, entry.getCard(), entry.getSourcePermanentId(), targetId,
                            effect, tokenCount, List.of());
            beginAttackTargetChoice(gameData, context);
            return;
        }
        tokenCopySupport.createTokenCopies(gameData, entry, Collections.nCopies(copyCount, targetPermanent.getCard()),
                sourcePermanent, tokenControllerId, effect);
    }

    private void beginAttackTargetChoice(GameData gameData,
                                         PermanentChoiceContext.CreateTokenCopiesAttacking context) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, context.controllerId());
        List<UUID> planeswalkerIds = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginAnyTargetChoice(gameData, context.controllerId(), planeswalkerIds,
                List.of(opponentId), "Choose the player or planeswalker for the next token to attack.");
    }
}
