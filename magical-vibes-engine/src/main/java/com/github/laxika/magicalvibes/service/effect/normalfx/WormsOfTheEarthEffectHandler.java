package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.WormsOfTheEarthEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Worms of the Earth's upkeep choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class WormsOfTheEarthEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WormsOfTheEarthEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = apnapPlayers(gameData);
        promptNext(gameData, entry.getCard(), new WormsOfTheEarthEffect(
                List.copyOf(players), entry.getControllerId(), entry.getSourcePermanentId(), false));
    }

    public void promptNext(GameData gameData, Card sourceCard, WormsOfTheEarthEffect effect) {
        if (effect.remainingPlayerIds().isEmpty()) {
            return;
        }

        UUID playerId = effect.remainingPlayerIds().getFirst();
        boolean hasTwoLands = landIds(gameData, playerId).size() >= 2;
        boolean damageChoice = effect.damageChoice() || !hasTwoLands;
        String prompt = damageChoice
                ? "Have " + sourceCard.getName() + " deal 5 damage to you? If you do, destroy it."
                : "Sacrifice two lands? If you do, destroy " + sourceCard.getName() + ".";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(new WormsOfTheEarthEffect(
                        effect.remainingPlayerIds(), effect.abilityControllerId(),
                        effect.sourcePermanentId(), damageChoice)),
                prompt,
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
    }

    public List<UUID> landIds(GameData gameData, UUID playerId) {
        List<UUID> result = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (playerId.equals(controllerId)
                    && gameQueryService.isLand(gameData, permanent)) {
                result.add(permanent.getId());
            }
        });
        return result;
    }

    public void beginLandChoice(GameData gameData, Card sourceCard, WormsOfTheEarthEffect effect,
                                UUID playerId, List<UUID> landIds) {
        playerInputService.beginMultiPermanentChoice(
                gameData,
                playerId,
                landIds,
                2,
                new MultiPermanentChoiceContext.WormsOfTheEarthSacrificeLands(
                        playerId, sourceCard, effect),
                "Choose two lands to sacrifice.");
    }

    public void sacrificeAndDestroy(GameData gameData, Card sourceCard,
                                    WormsOfTheEarthEffect effect, List<UUID> landIds,
                                    UUID playerId) {
        for (UUID landId : landIds) {
            Permanent land = gameQueryService.findPermanentById(gameData, landId);
            if (land != null && playerId.equals(gameQueryService.findPermanentController(gameData, landId))
                    && gameQueryService.isLand(gameData, land)) {
                destructionSupport.sacrificeAndLog(gameData, land, playerId);
            }
        }
        destroySource(gameData, sourceCard, effect.sourcePermanentId());
    }

    public void dealDamageAndDestroy(GameData gameData, Card sourceCard,
                                     WormsOfTheEarthEffect effect, UUID playerId) {
        DealDamageToPlayersEffect damage =
                new DealDamageToPlayersEffect(5, DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                effect.abilityControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                playerId,
                effect.sourcePermanentId());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " chooses to be dealt damage by ",
                sourceCard, "."));
        destroySource(gameData, sourceCard, effect.sourcePermanentId());
    }

    public void advance(GameData gameData, Card sourceCard, WormsOfTheEarthEffect effect,
                        UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new WormsOfTheEarthEffect(
                    List.copyOf(remaining), effect.abilityControllerId(), effect.sourcePermanentId(), false));
        }
    }

    private void destroySource(GameData gameData, Card sourceCard, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            destructionSupport.tryDestroyAndLog(gameData, source, sourceCard.getName());
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
