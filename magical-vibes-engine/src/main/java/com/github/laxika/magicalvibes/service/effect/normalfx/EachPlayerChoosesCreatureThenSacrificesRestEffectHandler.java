package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves each player's keep-one-creature choice and the resulting sacrifice sweep. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesCreatureThenSacrificesRestEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCreatureThenSacrificesRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (EachPlayerChoosesCreatureThenSacrificesRestEffect) effect;
        step(gameData, apnapPlayers(gameData), 0, List.of(), choiceEffect.counterType(),
                entry.getControllerId(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.EachPlayerChoosesCreatureThenSacrificesRestChoice context) {
        List<UUID> allChosenIds = new ArrayList<>(context.chosenIds());
        allChosenIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, allChosenIds,
                context.counterType(), context.controllerId(), context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
            List<UUID> chosenIds, CounterType counterType, UUID controllerId, String sourceName) {
        List<UUID> allChosenIds = new ArrayList<>(chosenIds);

        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, playerId,
                    ignored -> true);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                allChosenIds.add(candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, candidates, 1,
                    new MultiPermanentChoiceContext.EachPlayerChoosesCreatureThenSacrificesRestChoice(
                            playerIds, currentPlayerIndex, allChosenIds, counterType, controllerId, sourceName),
                    sourceName + " — choose a creature to put a " + counterType.name().toLowerCase()
                            + " counter on and keep.");
            return;
        }

        finish(gameData, allChosenIds, counterType, controllerId, sourceName);
    }

    private void finish(GameData gameData, List<UUID> chosenIds, CounterType counterType,
            UUID controllerId, String sourceName) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        PermanentNotPredicate exemption = new PermanentNotPredicate(
                new PermanentHasCountersPredicate(counterType));
        for (UUID chosenId : chosenIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
            if (chosen == null) {
                continue;
            }
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, chosen, counterType, 1);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceName, null, controllerId,
                    new CreaturesCantAttackControllerUnlessPredicateEffect(exemption, true, null),
                    chosenId, controllerId, null, EffectDuration.PERMANENT, 0));
        }

        Set<UUID> keptIds = new HashSet<>(chosenIds);
        List<UUID> toSacrifice = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !keptIds.contains(permanent.getId())) {
                    toSacrifice.add(permanent.getId());
                }
            }
        });
        if (!toSacrifice.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
