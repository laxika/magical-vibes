package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Speedbrood Stalker's private selection and sequential sacrifices. */
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID sourceControllerId = entry.getControllerId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, sourceControllerId)) {
            return;
        }

        List<UUID> selectableIds = matchingIds(gameData, targetPlayerId, false);
        if (selectableIds.isEmpty()) {
            return;
        }

        if (selectableIds.size() == 1) {
            beginTargetPlayerChoice(gameData, targetPlayerId, sourceControllerId,
                    selectableIds.getFirst(), entry.getCard().getName());
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                sourceControllerId,
                selectableIds,
                1,
                new MultiPermanentChoiceContext.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen(
                        targetPlayerId, sourceControllerId, null, entry.getCard().getName()),
                entry.getCard().getName() + " — secretly choose a creature or planeswalker.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen context) {
        UUID chosenPermanentId = permanentIds.getFirst();
        if (context.chosenPermanentId() == null) {
            beginTargetPlayerChoice(gameData, context.targetPlayerId(), context.sourceControllerId(),
                    chosenPermanentId, context.sourceCardName());
            return;
        }

        Permanent firstSacrifice = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (firstSacrifice != null
                && context.targetPlayerId().equals(
                        gameQueryService.findPermanentController(gameData, chosenPermanentId))
                && isSelectable(gameData, firstSacrifice, true)) {
            destructionSupport.sacrificeAndLog(gameData, firstSacrifice, context.targetPlayerId());
        }
        sacrificeChosenPermanent(gameData, context.targetPlayerId(), context.chosenPermanentId());
    }

    private void beginTargetPlayerChoice(GameData gameData, UUID targetPlayerId, UUID sourceControllerId,
                                         UUID chosenPermanentId, String sourceCardName) {
        List<UUID> sacrificeIds = matchingIds(gameData, targetPlayerId, true);
        if (sacrificeIds.isEmpty()) {
            sacrificeChosenPermanent(gameData, targetPlayerId, chosenPermanentId);
        } else if (sacrificeIds.size() == 1) {
            Permanent firstSacrifice = gameQueryService.findPermanentById(gameData, sacrificeIds.getFirst());
            if (firstSacrifice != null) {
                destructionSupport.sacrificeAndLog(gameData, firstSacrifice, targetPlayerId);
            }
            sacrificeChosenPermanent(gameData, targetPlayerId, chosenPermanentId);
        } else {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    targetPlayerId,
                    sacrificeIds,
                    1,
                    new MultiPermanentChoiceContext.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen(
                            targetPlayerId, sourceControllerId, chosenPermanentId, sourceCardName),
                    sourceCardName + " — choose a creature or planeswalker to sacrifice.");
        }
    }

    private void sacrificeChosenPermanent(GameData gameData, UUID targetPlayerId, UUID chosenPermanentId) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen != null
                && targetPlayerId.equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))
                && !gameQueryService.cantBeSacrificed(gameData, chosen)) {
            destructionSupport.sacrificeAndLog(gameData, chosen, targetPlayerId);
        }
    }

    private List<UUID> matchingIds(GameData gameData, UUID playerId, boolean sacrificeableOnly) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return ids;
        }
        for (Permanent permanent : battlefield) {
            if (isSelectable(gameData, permanent, sacrificeableOnly)) {
                ids.add(permanent.getId());
            }
        }
        return ids;
    }

    private boolean isSelectable(GameData gameData, Permanent permanent, boolean sacrificeableOnly) {
        return (gameQueryService.isCreature(gameData, permanent)
                || gameQueryService.isPlaneswalker(gameData, permanent))
                && (!sacrificeableOnly || !gameQueryService.cantBeSacrificed(gameData, permanent));
    }
}
