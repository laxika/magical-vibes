package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCreatureTheyControlTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentChoosesCreatureTheyControlTokenCopyEffect} (Echo Chamber): an opponent of
 * the controller chooses a creature they control and the controller gets a hasty token copy of it
 * that is exiled at the beginning of the next end step.
 *
 * <p>With 0 creatures nothing happens; with exactly 1 it is chosen automatically; with 2+ the
 * opponent picks via {@link PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpponentChoosesCreatureTheyControlTokenCopyEffectHandler implements NormalEffectHandlerBean {

    /** Haste until end of turn plus exile at the next end step — Echo Chamber's token profile. */
    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect(true, true);

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesCreatureTheyControlTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();

        // The controller picks which opponent chooses; in a two-player game there is exactly one, so
        // take the first opponent in turn order for determinism.
        UUID opponentId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                opponentId = playerId;
                break;
            }
        }
        if (opponentId == null) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        for (Permanent perm : gameData.playerBattlefields.getOrDefault(opponentId, List.of())) {
            if (gameQueryService.isCreature(gameData, perm)) {
                creatureIds.add(perm.getId());
            }
        }

        if (creatureIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " resolves but its controller's opponent controls no creature to copy."));
            log.info("Game {} - {} resolves with no creature for the opponent to choose",
                    gameData.id, sourceCard.getName());
            return;
        }

        if (creatureIds.size() == 1) {
            createCopy(gameData, controllerId, sourceCard, creatureIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy(
                        opponentId, controllerId, sourceCard));
        playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds,
                sourceCard.getName() + " — Choose a creature you control to be copied.");
    }

    /** Completion path for the 2+ creature prompt. */
    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy context) {
        createCopy(gameData, context.copyControllerId(), context.sourceCard(), permanentId);
    }

    /**
     * Reuses the token-copy handler by handing it a synthetic entry whose {@code targetId} is the
     * chosen creature, so the copy honours CR 707.2 copiable characteristics and the token
     * multiplier exactly as {@code CreateTokenCopyOfTargetPermanentEffect} does elsewhere.
     */
    private void createCopy(GameData gameData, UUID controllerId, Card sourceCard, UUID chosenPermanentId) {
        StackEntry copyEntry = new StackEntry(sourceCard, controllerId);
        copyEntry.setTargetId(chosenPermanentId);
        tokenCopyHandler.resolve(gameData, copyEntry, TOKEN_PROFILE);
    }
}
