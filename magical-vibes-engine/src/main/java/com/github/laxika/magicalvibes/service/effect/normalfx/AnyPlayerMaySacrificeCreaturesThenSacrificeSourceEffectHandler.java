package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves ETB abilities that let players sacrifice a fixed number of creatures to sacrifice the source. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect) effect;
        List<UUID> players = e.remainingPlayerIds() == null
                ? apnapPlayers(gameData)
                : new ArrayList<>(e.remainingPlayerIds());
        UUID abilityControllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        UUID sourcePermanentId = e.sourcePermanentId() != null
                ? e.sourcePermanentId()
                : entry.getSourcePermanentId();
        players.removeIf(playerId -> !gameData.playerIds.contains(playerId)
                || creatureIds(gameData, playerId, abilityControllerId).size() < e.count());
        if (players.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
                e.count(), List.copyOf(players), abilityControllerId, sourcePermanentId));
    }

    /** Returns the creatures a player can sacrifice for this effect. */
    public List<UUID> creatureIds(GameData gameData, UUID playerId, UUID abilityControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, abilityControllerId)) {
            return List.of();
        }
        return maySacrificeForCounterSupport.matchingPermanentIds(
                        gameData, playerId, new PermanentIsCreaturePredicate())
                .stream()
                .filter(permanentId -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                    return permanent != null && !gameQueryService.cantBeSacrificed(gameData, permanent);
                })
                .toList();
    }

    /** Enqueues the choice for the first remaining player. */
    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Sacrifice " + effect.count() + " creatures? If you do, sacrifice "
                        + sourceCard.getName() + ".",
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} creature-sacrifice choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    /** Returns the eligible players after the current player has answered. */
    public List<UUID> remainingAfter(GameData gameData,
                                     AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect,
                                     UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id)
                || creatureIds(gameData, id, effect.abilityControllerId()).size() < effect.count());
        return List.copyOf(remaining);
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.size());
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            return rotated;
        }
        return ordered;
    }
}
