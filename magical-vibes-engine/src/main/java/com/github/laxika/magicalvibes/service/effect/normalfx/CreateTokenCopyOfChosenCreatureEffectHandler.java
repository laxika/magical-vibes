package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Esix's non-targeting creature choice and reuses the shared token-copy pipeline. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfChosenCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfChosenCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfChosenCreatureEffect) effect;
        List<UUID> creatureIds = creatureIds(gameData, copyEffect.excludedPermanentId());
        if (creatureIds.isEmpty() || copyEffect.amount() <= 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " resolves but there is no other creature to copy."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.EsixCreatureChoice(
                entry.getControllerId(), entry.getCard(), entry, copyEffect.amount(),
                copyEffect.excludedPermanentId()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds,
                entry.getCard().getName() + " — Choose a creature other than Esix to copy.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EsixCreatureChoice context) {
        if (!creatureIds(gameData, context.excludedPermanentId()).contains(permanentId)) {
            return;
        }
        createCopies(gameData, context.resolvingEntry(), permanentId, context.amount());
    }

    private List<UUID> creatureIds(GameData gameData, UUID excludedPermanentId) {
        List<UUID> ids = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!permanent.getId().equals(excludedPermanentId)
                        && gameQueryService.isCreature(gameData, permanent)) {
                    ids.add(permanent.getId());
                }
            }
        }
        return ids;
    }

    private void createCopies(GameData gameData, StackEntry resolvingEntry,
                              UUID chosenPermanentId, int amount) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            return;
        }

        StackEntry copyEntry = new StackEntry(resolvingEntry.getCard(), resolvingEntry.getControllerId());
        copyEntry.setTargetId(chosenPermanentId);
        tokenCopyHandler.resolve(gameData, copyEntry,
                new CreateTokenCopyOfTargetPermanentEffect(new Fixed(amount)));
        resolvingEntry.getCreatedPermanentIds().addAll(copyEntry.getCreatedPermanentIds());
    }
}
