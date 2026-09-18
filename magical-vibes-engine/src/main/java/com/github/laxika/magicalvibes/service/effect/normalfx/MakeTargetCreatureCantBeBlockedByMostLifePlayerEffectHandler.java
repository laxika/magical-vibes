package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreatureCantBeBlockedByMostLifePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MakeTargetCreatureCantBeBlockedByMostLifePlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCreatureCantBeBlockedByMostLifePlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCreatureId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetCreatureId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int highestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .max()
                .orElse(0);
        List<UUID> tiedPlayers = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getLife(playerId) == highestLife)
                .toList();

        if (tiedPlayers.size() == 1) {
            applyRestriction(gameData, entry.getCard().getName(), entry.getControllerId(),
                    entry.getSourcePermanentId(), targetCreatureId, tiedPlayers.getFirst());
            return;
        }
        if (tiedPlayers.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.BlackGateMostLifeChoice(
                        entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                        targetCreatureId, tiedPlayers));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), tiedPlayers,
                entry.getCard().getName() + " — Choose a player tied for most life.");
    }

    public void completeChoice(GameData gameData, UUID chosenPlayerId,
                               PermanentChoiceContext.BlackGateMostLifeChoice context) {
        if (!context.eligiblePlayerIds().contains(chosenPlayerId)) {
            return;
        }
        applyRestriction(gameData, context.sourceCard().getName(), context.controllerId(),
                context.sourcePermanentId(), context.targetCreatureId(), chosenPlayerId);
    }

    private void applyRestriction(GameData gameData, String sourceCardName, UUID controllerId,
                                  UUID sourcePermanentId, UUID targetCreatureId, UUID chosenPlayerId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetCreatureId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceCardName, sourcePermanentId, controllerId,
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                        new PermanentControlledByPlayerPredicate(chosenPlayerId),
                        new PermanentIsSpecificPermanentPredicate(targetCreatureId),
                        "can't block this creature"),
                null, null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String playerName = gameData.playerIdToName.getOrDefault(chosenPlayerId, "that player");
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " can't be blocked by creatures controlled by " + playerName + " this turn."));
    }
}
