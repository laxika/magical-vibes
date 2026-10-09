package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainTargetPlayersLandManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Activates the target player's chosen land mana abilities, then transfers the resulting pool. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrainTargetPlayersLandManaEffectHandler implements NormalEffectHandlerBean {

    private final LandManaDrainSupport landManaDrainSupport;
    private final GameLogService gameLogService;
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
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || gameData.playerManaPools.get(targetPlayerId) == null
                || gameData.playerManaPools.get(entry.getControllerId()) == null) return;
        var lands = gameData.playerBattlefields.get(targetPlayerId).stream()
                .filter(land -> !land.isTapped() && gameQueryService.isLand(gameData, land))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
        continueDrain(gameData, entry, lands);
    }

    /** Resumes after all choices belonging to the preceding mana ability have completed. */
    public void continueDrain(GameData gameData, StackEntry entry, java.util.List<UUID> remainingLandIds) {
        UUID playerId = entry.getTargetId();
        for (int position = 0; position < remainingLandIds.size(); position++) {
            UUID landId = remainingLandIds.get(position);
            var land = gameQueryService.findPermanentById(gameData, landId);
            if (land == null || land.isTapped() || !gameData.playerBattlefields.get(playerId).contains(land)
                    || !gameQueryService.canActivateManaAbility(gameData, land)) continue;
            var indices = new java.util.ArrayList<Integer>();
            var abilities = abilityActivationService.getEffectiveActivatedAbilities(gameData, land);
            var options = new java.util.ArrayList<String>();
            if (com.github.laxika.magicalvibes.service.cast.PotentialManaService.hasOnTapManaEffects(land.getCard())
                    && !gameQueryService.hasLostPrintedAbilities(gameData, land)) {
                indices.add(-1);
                options.add("Tap for mana");
            }
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
                        entry, landId, indices, remaining), options, "Choose a mana ability of " + land.getCard().getName() + "."));
                return;
            }
            activateLandAbility(gameData, entry, landId, indices.getFirst(), remaining);
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

    /** Activates a selected mana ability through the ordinary cost and mana-production path. */
    public void activateLandAbility(GameData gameData, StackEntry entry, UUID landId,
                                    int abilityIndex, java.util.List<UUID> remainingLandIds) {
        UUID playerId = entry.getTargetId();
        var battlefield = gameData.playerBattlefields.get(playerId);
        var land = gameQueryService.findPermanentById(gameData, landId);
        int permanentIndex = battlefield.indexOf(land);
        if (permanentIndex < 0) {
            continueDrain(gameData, entry, remainingLandIds);
            return;
        }
        var continuation = new com.github.laxika.magicalvibes.model.PendingInteraction.LandManaDrainContinuation(
                entry, remainingLandIds);
        gameData.pendingInteractions.addLast(continuation);
        var player = new com.github.laxika.magicalvibes.model.Player(playerId,
                gameData.playerNames.get(gameData.playerIds.indexOf(playerId)));
        if (abilityIndex == -1) abilityActivationService.tapPermanent(gameData, player, permanentIndex);
        else abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, null, null, null);
        if (!gameData.interaction.isAwaitingInput()) {
            gameData.pendingInteractions.remove(continuation);
            continueDrain(gameData, entry, remainingLandIds);
        }
    }
}
