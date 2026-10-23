package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLandsUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves Cleansing's independent pay-life-or-destroy decision for every land; the unprotected
 * lands are destroyed together after the last decision.
 *
 * <p>Nothing changes before that destruction while the spell resolves without a prompt, so a land
 * creature with competing death replacements stops and reruns the effect (CR 616.1). After a
 * prompt the life already paid can't be paid again, so the final destruction alone is asked about
 * first and redone through a declined prompt without a current land, which only finishes the
 * sequence.
 */
@Component
@RequiredArgsConstructor
public class DestroyLandsUnlessAnyPlayerPaysLifeEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyLandsUnlessAnyPlayerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cleansing = (DestroyLandsUnlessAnyPlayerPaysLifeEffect) effect;
        if (cleansing.currentLandId() != null) {
            offerOrDestroyCurrentLand(gameData, entry.getCard(), cleansing, null);
            return;
        }

        List<UUID> landIds = cleansing.remainingLandIds().isEmpty()
                ? landsInApnapOrder(gameData)
                : cleansing.remainingLandIds();
        beginNextLand(gameData, entry.getCard(), cleansing.lifeCost(), landIds, cleansing.unprotectedLandIds(),
                null);
    }

    /**
     * Continues the sequence after {@code answeringPlayerId} answered the prompt for the current
     * land, {@code paid} saying whether the land is now protected, and finishes the answered prompt.
     */
    public void continueAfterDecision(GameData gameData, DestroyLandsUnlessAnyPlayerPaysLifeEffect effect,
                                      Card sourceCard, boolean paid, UUID answeringPlayerId) {
        if (paid) {
            beginNextLand(gameData, sourceCard, effect.lifeCost(), effect.remainingLandIds(), effect.unprotectedLandIds(),
                    answeringPlayerId);
            return;
        }

        offerOrDestroyCurrentLand(gameData, sourceCard, effect, answeringPlayerId);
    }

    public boolean canPayLife(GameData gameData, UUID playerId, int lifeCost) {
        return gameData.playerIds.contains(playerId)
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameQueryService.canPlayerLoseLife(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
    }

    /**
     * Offers the next land to the players, or destroys the unprotected lands once none is left.
     * {@code answeringPlayerId} is the player whose answer to a prompt got here, or {@code null}
     * while the spell is still resolving.
     */
    private void beginNextLand(GameData gameData, Card sourceCard,
                               int lifeCost, List<UUID> landIds, List<UUID> unprotectedLandIds,
                               UUID answeringPlayerId) {
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
                finishAnsweredPrompt(gameData, answeringPlayerId);
                return;
            }
            unprotected.add(landId);
        }
        finish(gameData, sourceCard, lifeCost, unprotected, answeringPlayerId);
    }

    private void offerOrDestroyCurrentLand(GameData gameData,
                                           Card sourceCard,
                                           DestroyLandsUnlessAnyPlayerPaysLifeEffect effect,
                                           UUID answeringPlayerId) {
        Permanent land = gameQueryService.findPermanentById(gameData, effect.currentLandId());
        if (land != null && offerNextPayer(gameData, sourceCard, effect)) {
            finishAnsweredPrompt(gameData, answeringPlayerId);
            return;
        }
        List<UUID> unprotected = new ArrayList<>(effect.unprotectedLandIds());
        if (land != null) {
            unprotected.add(effect.currentLandId());
        }
        beginNextLand(gameData, sourceCard, effect.lifeCost(), effect.remainingLandIds(), unprotected,
                answeringPlayerId);
    }

    private void finishAnsweredPrompt(GameData gameData, UUID answeringPlayerId) {
        if (answeringPlayerId != null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void finish(GameData gameData, Card sourceCard, int lifeCost, List<UUID> unprotectedLandIds,
                        UUID answeringPlayerId) {
        if (answeringPlayerId == null) {
            // Still inside the spell's own resolution, whose rerun window covers the destruction.
            destroyLands(gameData, sourceCard, unprotectedLandIds);
            return;
        }
        // Reached via an answered prompt: the destruction is asked about first (CR 616.1) and redone by
        // declining a prompt without a current land, which leads straight back here.
        PendingMayAbility finishOnly = new PendingMayAbility(sourceCard, answeringPlayerId,
                List.of(new DestroyLandsUnlessAnyPlayerPaysLifeEffect(
                        lifeCost, List.of(), null, List.of(), unprotectedLandIds)),
                "");
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(gameData,
                new Player(answeringPlayerId, gameData.playerIdToName.get(answeringPlayerId)), finishOnly, false,
                () -> {
                    destroyLands(gameData, sourceCard, unprotectedLandIds);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                });
    }

    private void destroyLands(GameData gameData, Card sourceCard, List<UUID> landIds) {
        destructionSupport.destroyBatch(gameData, landIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull).toList(), sourceCard.getName(), false);
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
