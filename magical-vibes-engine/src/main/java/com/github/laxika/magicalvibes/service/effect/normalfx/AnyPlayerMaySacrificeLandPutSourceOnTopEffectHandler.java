package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the Argothian Wurm enter-the-battlefield land choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeLandPutSourceOnTopEffectHandler implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PutTargetOnTopOfLibraryEffectHandler putTargetOnTopOfLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeLandPutSourceOnTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = apnapPlayers(gameData);
        players.removeIf(playerId -> landIds(gameData, playerId).isEmpty());
        if (players.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyPlayerMaySacrificeLandPutSourceOnTopEffect(
                List.copyOf(players), entry.getControllerId(), entry.getSourcePermanentId()));
    }

    public List<UUID> landIds(GameData gameData, UUID playerId) {
        return maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, playerId, new PermanentIsLandPredicate());
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMaySacrificeLandPutSourceOnTopEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Sacrifice a land? If you do, put " + sourceCard.getName()
                        + " on top of its owner's library.",
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} land sacrifice choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    public void sacrificeLand(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        var land = gameQueryService.findPermanentById(gameData, permanentId);
        if (land != null) {
            destructionSupport.sacrificeAndLog(gameData, land, sacrificingPlayerId);
        }
    }

    public void putSourceOnTop(GameData gameData, Card sourceCard,
                               AnyPlayerMaySacrificeLandPutSourceOnTopEffect effect) {
        if (effect.sourcePermanentId() == null) {
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                effect.abilityControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(PutTargetOnTopOfLibraryEffect.self())),
                (UUID) null,
                effect.sourcePermanentId());
        putTargetOnTopOfLibraryEffectHandler.resolve(
                gameData, entry, PutTargetOnTopOfLibraryEffect.self());
    }

    public void advance(GameData gameData, Card sourceCard,
                         AnyPlayerMaySacrificeLandPutSourceOnTopEffect effect,
                         UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        remaining.removeIf(id -> landIds(gameData, id).isEmpty());

        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyPlayerMaySacrificeLandPutSourceOnTopEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.sourcePermanentId()));
        }
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
