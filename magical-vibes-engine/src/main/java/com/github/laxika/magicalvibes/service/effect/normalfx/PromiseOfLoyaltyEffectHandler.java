package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PromiseOfLoyaltyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves Promise of Loyalty's per-player keep-one creature choices. */
@Component
@RequiredArgsConstructor
public class PromiseOfLoyaltyEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PromiseOfLoyaltyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, entry, apnapPlayers(gameData), 0, Map.of());
    }

    /** Completes one player's choice and advances the APNAP sequence. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.PromiseOfLoyaltyChoice context) {
        Map<UUID, UUID> chosenByPlayer = new LinkedHashMap<>(context.chosenByPlayer());
        chosenByPlayer.put(context.playerIds().get(context.playerIndex()), chosenIds.getFirst());
        step(gameData, context.resolvingEntry(), context.playerIds(), context.playerIndex() + 1,
                chosenByPlayer);
    }

    private void step(GameData gameData, StackEntry entry, List<UUID> playerIds, int playerIndex,
                      Map<UUID, UUID> chosenByPlayer) {
        Map<UUID, UUID> choices = new LinkedHashMap<>(chosenByPlayer);

        while (playerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(playerIndex);
            int currentIndex = playerIndex++;

            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<UUID> candidates = destructionSupport.collectCreatureIds(gameData, playerId,
                    ignored -> true);
            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                choices.put(playerId, candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    playerId,
                    candidates,
                    1,
                    new MultiPermanentChoiceContext.PromiseOfLoyaltyChoice(
                            playerIds, currentIndex, choices, entry),
                    "Promise of Loyalty — choose a creature to keep.");
            return;
        }

        completeAfterChoices(gameData, entry, playerIds, choices);
    }

    private void completeAfterChoices(GameData gameData, StackEntry entry, List<UUID> playerIds,
                                      Map<UUID, UUID> chosenByPlayer) {
        Set<UUID> keptIds = new HashSet<>(chosenByPlayer.values());
        for (UUID keptId : keptIds) {
            Permanent kept = gameQueryService.findPermanentById(gameData, keptId);
            if (kept == null) {
                continue;
            }
            int placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, kept, CounterType.VOW, 1);
            if (placed > 0) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(),
                        entry.getCard().getName(),
                        null,
                        entry.getControllerId(),
                        new EnchantedCreatureCantAttackControllerEffect(),
                        kept.getId(),
                        null,
                        null,
                        EffectDuration.PERMANENT,
                        0));
            }
        }

        List<UUID> toSacrifice = new ArrayList<>();
        for (UUID playerId : playerIds) {
            if (!chosenByPlayer.containsKey(playerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!keptIds.contains(permanent.getId())
                        && gameQueryService.isCreature(gameData, permanent)) {
                    toSacrifice.add(permanent.getId());
                }
            }
        }

        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
        permanentRemovalService.removeOrphanedAuras(gameData);
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
