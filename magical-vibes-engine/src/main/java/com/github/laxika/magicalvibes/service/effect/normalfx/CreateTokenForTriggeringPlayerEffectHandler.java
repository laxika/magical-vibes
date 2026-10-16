package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token creation effect for the player whose action caused the trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenForTriggeringPlayerEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenForTriggeringPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenForTriggeringPlayerEffect) effect;
        UUID playerId = entry.getTriggeringPermanentControllerId() != null
                ? entry.getTriggeringPermanentControllerId()
                : entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        if (e.resolveCurrentController() && entry.getTriggeringPermanentId() != null) {
            UUID currentController = gameQueryService.findPermanentController(
                    gameData, entry.getTriggeringPermanentId());
            if (currentController != null) playerId = currentController;
        }
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, e.token().amount(), context);
        if (amount <= 0) {
            return;
        }
        if (e.token().tappedAndAttacking() && beginAttackTargetChoice(gameData, entry, playerId, e, amount)) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, e.token().power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, e.token().toughness(), context);
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, playerId, e.token(), amount, entry.getCard().getSetCode(), power, toughness);
        entry.getCreatedPermanentIds().addAll(createdIds);
        if (e.token().tappedAndAttacking() && entry.getAttackedTargetId() != null) {
            for (UUID createdId : createdIds) {
                Permanent token = gameQueryService.findPermanentById(gameData, createdId);
                if (token != null) {
                    token.setAttackTarget(entry.getAttackedTargetId());
                }
            }
        }
    }

    /**
     * Tokens that enter attacking are attacking whatever their controller chooses (CR 508.4), so when
     * the controller has more than one player or planeswalker to attack the choice is theirs. Returns
     * whether a choice was started; with a single possible defender the tokens just attack it.
     */
    private boolean beginAttackTargetChoice(GameData gameData, StackEntry entry, UUID playerId,
                                            CreateTokenForTriggeringPlayerEffect e, int amount) {
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(playerId))
                .toList();
        List<UUID> planeswalkerIds = opponentIds.stream()
                .flatMap(opponentId -> gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream())
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (opponentIds.size() + planeswalkerIds.size() <= 1) {
            return false;
        }
        int tokenCount = gameQueryService.getTokenCreationAmount(
                gameData, playerId, amount, e.token().subtypes(), e.token().primaryType() == CardType.CREATURE);
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CreateTokensAttacking(
                playerId, entry.getCard(), e.token(), amount, tokenCount, false, List.of()));
        playerInputService.beginAnyTargetChoice(gameData, playerId, planeswalkerIds, opponentIds,
                "Choose the player or planeswalker for the next " + e.token().tokenName() + " token to attack.");
        return true;
    }
}
