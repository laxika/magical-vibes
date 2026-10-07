package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSaddledCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Calamity's repeated saddled-creature copy choices. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfSaddledCreatureEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect(false, false, true, true);
    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE_WITH_ATTACK_CHOICE =
            new CreateTokenCopyOfTargetPermanentEffect(
                    List.of(), Set.of(), null, null, Map.of(), false, false, true, true,
                    false, false, null, Set.of(), false, Map.of(), List.of(), false, false,
                    new Fixed(1), false, Set.of(), true);

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfSaddledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copies = (CreateTokenCopiesOfSaddledCreatureEffect) effect;
        beginChoice(gameData, entry, copies.amount());
    }

    public void completeChoice(GameData gameData, List<UUID> selectedIds,
                               MultiPermanentChoiceContext.CreateTokenCopiesOfSaddledCreature context) {
        if (!selectedIds.isEmpty() && copyAttacking(gameData, context, selectedIds.getFirst())) {
            return;
        }
        continueIterations(gameData, context);
    }

    /** Repeats the copy process for the remaining iterations ("Repeat this process once"). */
    public void continueIterations(GameData gameData,
                                   MultiPermanentChoiceContext.CreateTokenCopiesOfSaddledCreature context) {
        int remainingIterations = context.remainingIterations() - 1;
        if (remainingIterations > 0) {
            beginChoice(gameData, context.resolvingEntry(), remainingIterations);
        }
    }

    /**
     * Creates the tapped and attacking copy. Its controller chooses which player or planeswalker it
     * attacks when there is more than one option; returns {@code true} while that choice is pending.
     */
    private boolean copyAttacking(GameData gameData,
                                  MultiPermanentChoiceContext.CreateTokenCopiesOfSaddledCreature context,
                                  UUID saddlerId) {
        StackEntry entry = context.resolvingEntry();
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        List<UUID> planeswalkerIds = opponentIds.stream()
                .flatMap(opponentId -> gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream())
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (opponentIds.size() + planeswalkerIds.size() <= 1
                || gameQueryService.findPermanentById(gameData, saddlerId) == null) {
            tokenCopyHandler.resolveForTarget(gameData, entry, TOKEN_PROFILE, saddlerId);
            return false;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CreateTokenCopiesAttacking(
                entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), saddlerId,
                TOKEN_PROFILE_WITH_ATTACK_CHOICE, 1, List.of(), context));
        playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), planeswalkerIds, opponentIds,
                "Choose the player or planeswalker for the token to attack.");
        return true;
    }

    private void beginChoice(GameData gameData, StackEntry entry, int remainingIterations) {
        List<UUID> validIds = validSaddlerIds(gameData, entry.getSourcePermanentId());
        if (validIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                validIds,
                1,
                new MultiPermanentChoiceContext.CreateTokenCopiesOfSaddledCreature(
                        entry, remainingIterations),
                entry.getCard().getName()
                        + " — Choose a nonlegendary creature that saddled it this turn to copy.");
    }

    public List<UUID> validSaddlerIds(GameData gameData, UUID sourceId) {
        if (sourceId == null) {
            return List.of();
        }
        Set<UUID> saddlerIds = gameData.creaturesThatSaddledPermanentThisTurn
                .getOrDefault(sourceId, Set.of());
        return saddlerIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null
                        && gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.hasEffectiveSupertype(
                                gameData, permanent, CardSupertype.LEGENDARY))
                .map(Permanent::getId)
                .toList();
    }
}
