package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureAndPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the delirium branch of To the Slaughter. */
@Component
@RequiredArgsConstructor
public class TargetPlayerSacrificesCreatureAndPlaneswalkerEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesCreatureAndPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<UUID> planeswalkerIds = new ArrayList<>();
        List<UUID> candidateIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                boolean creature = gameQueryService.isCreature(gameData, permanent);
                boolean planeswalker = gameQueryService.isPlaneswalker(gameData, permanent);
                if (creature) {
                    creatureIds.add(permanent.getId());
                }
                if (planeswalker) {
                    planeswalkerIds.add(permanent.getId());
                }
                if (creature || planeswalker) {
                    candidateIds.add(permanent.getId());
                }
            }
        }

        if (candidateIds.isEmpty()) {
            return;
        }

        boolean canSacrificeTwo = creatureIds.stream()
                .anyMatch(creatureId -> planeswalkerIds.stream().anyMatch(planeswalkerId ->
                        !creatureId.equals(planeswalkerId)));
        int requiredCount = canSacrificeTwo ? 2 : 1;
        if (candidateIds.size() <= requiredCount) {
            destructionSupport.performSimultaneousSacrifice(gameData, candidateIds);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, candidateIds, requiredCount,
                new MultiPermanentChoiceContext.TargetPlayerSacrificesCreatureAndPlaneswalker(
                        targetPlayerId, creatureIds, planeswalkerIds, requiredCount, entry.getCard().getName()),
                "Choose " + requiredCount + " permanents to sacrifice.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.TargetPlayerSacrificesCreatureAndPlaneswalker context) {
        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
    }
}
