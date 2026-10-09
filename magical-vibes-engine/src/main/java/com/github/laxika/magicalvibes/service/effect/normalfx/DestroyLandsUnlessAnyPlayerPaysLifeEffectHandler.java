package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLandsUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Cleansing's independent pay-life-or-destroy decision for every land. */
@Component
@RequiredArgsConstructor
public class DestroyLandsUnlessAnyPlayerPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyLandsUnlessAnyPlayerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cleansing = (DestroyLandsUnlessAnyPlayerPaysLifeEffect) effect;
        if (cleansing.currentLandId() != null) {
            offerOrDestroyCurrentLand(gameData, entry.getCard(), cleansing);
            return;
        }

        List<UUID> landIds = cleansing.remainingLandIds().isEmpty()
                ? landsInApnapOrder(gameData)
                : cleansing.remainingLandIds();
        beginNextLand(gameData, entry.getCard(), cleansing.lifeCost(), landIds, cleansing.unprotectedLandIds());
    }

    public void continueAfterDecision(GameData gameData, DestroyLandsUnlessAnyPlayerPaysLifeEffect effect,
                                      Card sourceCard, boolean paid) {
        if (paid) {
            beginNextLand(gameData, sourceCard, effect.lifeCost(), effect.remainingLandIds(), effect.unprotectedLandIds());
            return;
        }

        offerOrDestroyCurrentLand(gameData, sourceCard, effect);
    }

    public boolean canPayLife(GameData gameData, UUID playerId, int lifeCost) {
        return gameData.playerIds.contains(playerId)
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameQueryService.canPlayerLoseLife(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
    }

    private void beginNextLand(GameData gameData, Card sourceCard,
                               int lifeCost, List<UUID> landIds, List<UUID> unprotectedLandIds) {
        List<UUID> remainingLandIds = new ArrayList<>(landIds);
        List<UUID> unprotected = new ArrayList<>(unprotectedLandIds);
        while (!remainingLandIds.isEmpty()) {
            UUID landId = remainingLandIds.removeFirst();
            Permanent land = gameQueryService.findPermanentById(gameData, landId);
            if (land == null) {
                continue;
            }

            var effect = new DestroyLandsUnlessAnyPlayerPaysLifeEffect(
                    lifeCost, remainingLandIds, landId, apnapOrder(gameData), unprotected);
            if (offerNextPayer(gameData, sourceCard, effect)) {
                return;
            }
            unprotected.add(landId);
        }
        destructionSupport.destroyBatch(gameData, unprotected.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull).toList(), sourceCard.getName(), false);
    }

    private void offerOrDestroyCurrentLand(GameData gameData,
                                           Card sourceCard,
                                           DestroyLandsUnlessAnyPlayerPaysLifeEffect effect) {
        Permanent land = gameQueryService.findPermanentById(gameData, effect.currentLandId());
        if (land != null && offerNextPayer(gameData, sourceCard, effect)) {
            return;
        }
        List<UUID> unprotected = new ArrayList<>(effect.unprotectedLandIds());
        if (land != null) {
            unprotected.add(effect.currentLandId());
        }
        beginNextLand(gameData, sourceCard, effect.lifeCost(), effect.remainingLandIds(), unprotected);
    }

    private boolean offerNextPayer(GameData gameData,
                                   Card sourceCard,
                                   DestroyLandsUnlessAnyPlayerPaysLifeEffect effect) {
        List<UUID> payerIds = effect.remainingPayerIds();
        for (int i = 0; i < payerIds.size(); i++) {
            UUID payerId = payerIds.get(i);
            if (!canPayLife(gameData, payerId, effect.lifeCost())) {
                continue;
            }
            Permanent land = gameQueryService.findPermanentById(gameData, effect.currentLandId());
            if (land == null) {
                return false;
            }
            var nextEffect = new DestroyLandsUnlessAnyPlayerPaysLifeEffect(
                    effect.lifeCost(), effect.remainingLandIds(), effect.currentLandId(),
                    payerIds.subList(i + 1, payerIds.size()), effect.unprotectedLandIds());
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard,
                    payerId,
                    List.of(nextEffect),
                    "Pay " + effect.lifeCost() + " life to prevent " + land.getCard().getName()
                            + " from being destroyed? (" + sourceCard.getName() + ")",
                    effect.currentLandId()));
            return true;
        }
        return false;
    }

    private List<UUID> landsInApnapOrder(GameData gameData) {
        List<UUID> landIds = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (gameQueryService.isLand(gameData, permanent)) {
                    landIds.add(permanent.getId());
                }
            }
        }
        return landIds;
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.size());
        rotated.addAll(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
