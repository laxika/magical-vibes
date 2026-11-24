package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainTargetPlayersLandManaEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Activates a mana ability of each land the target player controls through the ordinary mana
 * ability paths, waiting for each one's choices (the ability, its color, the order of land-mana
 * replacements), then transfers the resulting pool (Drain Power) or makes that player lose it for
 * delayed {C} (Pygmy Hippo).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrainTargetPlayersLandManaEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.ability.AbilityActivationService abilityActivationService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.github.laxika.magicalvibes.service.battlefield.GameQueryService gameQueryService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrainTargetPlayersLandManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var drain = (DrainTargetPlayersLandManaEffect) effect;
        UUID playerId = subjectResolutionService.players(gameData, entry, effect, drain.player()).stream()
                .findFirst().orElse(null);
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || gameData.playerManaPools.get(playerId) == null
                || (!drain.delayedColorless() && gameData.playerManaPools.get(entry.getControllerId()) == null)) {
            return;
        }
        var lands = gameData.playerBattlefields.get(playerId).stream()
                .filter(land -> !land.isTapped() && gameQueryService.isLand(gameData, land))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
        continueDrain(gameData, entry, playerId, lands, drain.delayedColorless());
    }

    /** Resumes after all choices belonging to the preceding mana ability have completed. */
    public void continueDrain(GameData gameData, StackEntry entry, UUID playerId,
                              java.util.List<UUID> remainingLandIds, boolean delayedColorless) {
        for (int position = 0; position < remainingLandIds.size(); position++) {
            UUID landId = remainingLandIds.get(position);
            var land = gameQueryService.findPermanentById(gameData, landId);
            if (land == null || land.isTapped() || !gameData.playerBattlefields.get(playerId).contains(land)
                    || !gameQueryService.canActivateManaAbility(gameData, land)) continue;
            var indices = new java.util.ArrayList<Integer>();
            var abilities = abilityActivationService.getEffectiveActivatedAbilities(gameData, land);
            var options = new java.util.ArrayList<String>();
            for (int index = 0; index < abilities.size(); index++) {
                var ability = abilities.get(index);
                if (ability.isRequiresTap()
                        && com.github.laxika.magicalvibes.service.ability.AbilityActivationService.isManaAbility(ability)
                        && abilityActivationService.canActivateAbility(gameData, playerId, land, index,
                        gameData.playerManaPools.get(playerId))) {
                    indices.add(index);
                    options.add(ability.getDescription());
                }
            }
            if (indices.isEmpty()) continue;
            var remaining = remainingLandIds.subList(position + 1, remainingLandIds.size());
            if (indices.size() > 1) {
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        playerId, landId, null, new com.github.laxika.magicalvibes.model.ChoiceContext.LandManaDrainAbilityChoice(
                        entry, playerId, landId, indices, remaining, delayedColorless), options,
                        "Choose a mana ability of " + FaceDownVisibility.publicName(gameData, land) + "."));
                return;
            }
            activateLandAbility(gameData, entry, playerId, landId, indices.getFirst(), remaining, delayedColorless);
            return;
        }
        if (delayedColorless) {
            loseForDelayedColorless(gameData, entry, playerId);
            return;
        }
        ManaPool targetPool = gameData.playerManaPools.get(playerId);
        ManaPool controllerPool = gameData.playerManaPools.get(entry.getControllerId());
        int totalTransferred = targetPool.getTotalAllMana();
        if (!playerId.equals(entry.getControllerId())) {
            controllerPool.addManaFrom(targetPool);
            targetPool.clear();
            targetPool.clearPersistentMana();
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" drains " + totalTransferred + " mana.").build());
    }

    private void loseForDelayedColorless(GameData gameData, StackEntry entry, UUID playerId) {
        ManaPool defendingPool = gameData.playerManaPools.get(playerId);
        int manaLost = defendingPool.getTotalAllMana();
        defendingPool.clear();
        defendingPool.clearPersistentMana();
        if (manaLost > 0) {
            gameData.temporaryGlobalTriggeredAbilities.add(
                    DelayedManaSupport.delayedMana(entry.getControllerId(),
                            entry.getCard(), entry.getSourcePermanentId(), ManaColor.COLORLESS, manaLost,
                            false, true, false, false));
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" drains " + manaLost + " mana for colorless at next main phase.").build());
    }

    /** Activates a selected mana ability through the ordinary cost and mana-production path. */
    public void activateLandAbility(GameData gameData, StackEntry entry, UUID playerId, UUID landId,
                                    int abilityIndex, java.util.List<UUID> remainingLandIds,
                                    boolean delayedColorless) {
        var battlefield = gameData.playerBattlefields.get(playerId);
        var land = gameQueryService.findPermanentById(gameData, landId);
        int permanentIndex = battlefield.indexOf(land);
        if (permanentIndex < 0) {
            continueDrain(gameData, entry, playerId, remainingLandIds, delayedColorless);
            return;
        }
        var continuation = new com.github.laxika.magicalvibes.model.PendingInteraction.LandManaDrainContinuation(
                entry, playerId, remainingLandIds, delayedColorless);
        gameData.pendingInteractions.addLast(continuation);
        var player = new com.github.laxika.magicalvibes.model.Player(playerId,
                gameData.playerIdToName.get(playerId));
        abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, null, null, null);
        if (!gameData.interaction.isAwaitingInput()) {
            gameData.pendingInteractions.remove(continuation);
            continueDrain(gameData, entry, playerId, remainingLandIds, delayedColorless);
        }
    }
}
