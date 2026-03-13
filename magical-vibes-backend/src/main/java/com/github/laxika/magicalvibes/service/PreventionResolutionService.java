package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreaturesCantBeTargetedByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreventionResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @HandlesEffect(PreventDamageToTargetEffect.class)
    void resolvePreventDamageToTarget(GameData gameData, StackEntry entry, PreventDamageToTargetEffect prevent) {
        UUID targetId = entry.getTargetPermanentId();

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + prevent.amount());

            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + target.getCard().getName() + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, prevent.amount(), target.getCard().getName());
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + prevent.amount());

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + playerName + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, prevent.amount(), playerName);
        }
    }

    @HandlesEffect(PreventNextDamageEffect.class)
    void resolvePreventNextDamage(GameData gameData, StackEntry entry, PreventNextDamageEffect prevent) {
        gameData.globalDamagePreventionShield += prevent.amount();

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to any permanent or player is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, prevent.amount());
    }

    @HandlesEffect(PreventAllCombatDamageEffect.class)
    void resolvePreventAllCombatDamage(GameData gameData, StackEntry entry) {
        gameData.preventAllCombatDamage = true;

        String logEntry = "All combat damage will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(PreventAllDamageToControllerAndCreaturesEffect.class)
    void resolvePreventAllDamageToControllerAndCreatures(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithAllDamagePrevented.add(controllerId);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "All damage that would be dealt to " + playerName + " and creatures " + playerName + " controls this turn is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(PreventDamageFromColorsEffect.class)
    void resolvePreventDamageFromColors(GameData gameData, StackEntry entry, PreventDamageFromColorsEffect effect) {
        gameData.preventDamageFromColors.addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " and " + b)
                .orElse("");
        String logEntry = "All damage from " + colorNames + " sources will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(PreventNextColorDamageToControllerEffect.class)
    void resolvePreventNextColorDamageToController(GameData gameData, StackEntry entry, PreventNextColorDamageToControllerEffect effect) {
        CardColor chosenColor = effect.chosenColor();
        if (chosenColor == null) return;

        UUID controllerId = entry.getControllerId();
        gameData.playerColorDamagePreventionCount
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(chosenColor, 1, Integer::sum);
    }

    @HandlesEffect(PreventAllDamageByTargetCreatureEffect.class)
    void resolvePreventAllDamageByTargetCreatures(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetPermanentIds();
        if (targetIds == null || targetIds.isEmpty()) return;

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            gameData.permanentsPreventedFromDealingDamage.add(targetId);
            String logEntry = "All damage " + target.getCard().getName() + " would deal this turn is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} prevented from dealing damage this turn", gameData.id, target.getCard().getName());
        }
    }

    @HandlesEffect(PermanentsEnterTappedThisTurnEffect.class)
    void resolvePermanentsEnterTappedThisTurn(GameData gameData, StackEntry entry) {
        gameData.allPermanentsEnterTappedThisTurn = true;

        String logEntry = "Permanents enter tapped this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(GrantControllerSpellsCantBeCounteredByColorsEffect.class)
    void resolveGrantControllerSpellsCantBeCounteredByColors(GameData gameData, StackEntry entry, GrantControllerSpellsCantBeCounteredByColorsEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playerSpellsCantBeCounteredByColorsThisTurn
                .computeIfAbsent(controllerId, k -> ConcurrentHashMap.newKeySet())
                .addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " or " + b)
                .orElse("");
        String logEntry = "Spells " + gameData.playerIdToName.get(controllerId) + " controls can't be countered by " + colorNames + " spells this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(GrantControllerCreaturesCantBeTargetedByColorsEffect.class)
    void resolveGrantControllerCreaturesCantBeTargetedByColors(GameData gameData, StackEntry entry, GrantControllerCreaturesCantBeTargetedByColorsEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playerCreaturesCantBeTargetedByColorsThisTurn
                .computeIfAbsent(controllerId, k -> ConcurrentHashMap.newKeySet())
                .addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " or " + b)
                .orElse("");
        String logEntry = "Creatures " + gameData.playerIdToName.get(controllerId) + " controls can't be the targets of " + colorNames + " spells this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    @HandlesEffect(PreventAllDamageFromChosenSourceEffect.class)
    void resolvePreventAllDamageFromChosenSource(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Collect all permanents on all battlefields as valid source choices
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> validIds.add(perm.getId()));

        if (validIds.isEmpty()) {
            String logEntry = "No permanents on the battlefield to choose as a damage source.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.PreventDamageSourceChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. Prevent all damage it would deal to you this turn.");
    }
}
