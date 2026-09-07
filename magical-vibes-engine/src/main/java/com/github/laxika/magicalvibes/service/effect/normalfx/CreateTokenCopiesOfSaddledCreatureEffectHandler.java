package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSaddledCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
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
        if (!selectedIds.isEmpty()) {
            tokenCopyHandler.resolveForTarget(
                    gameData, context.resolvingEntry(), TOKEN_PROFILE, selectedIds.getFirst());
        }

        int remainingIterations = context.remainingIterations() - 1;
        if (remainingIterations > 0) {
            beginChoice(gameData, context.resolvingEntry(), remainingIterations);
        }
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
