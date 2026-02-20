package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreventionResolutionService implements EffectHandlerProvider {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(PreventDamageToTargetEffect.class,
                (gd, entry, effect) -> resolvePreventDamageToTarget(gd, entry, (PreventDamageToTargetEffect) effect));
        registry.register(PreventNextDamageEffect.class,
                (gd, entry, effect) -> resolvePreventNextDamage(gd, (PreventNextDamageEffect) effect));
        registry.register(PreventAllCombatDamageEffect.class,
                (gd, entry, effect) -> resolvePreventAllCombatDamage(gd));
        registry.register(PreventDamageFromColorsEffect.class,
                (gd, entry, effect) -> resolvePreventDamageFromColors(gd, (PreventDamageFromColorsEffect) effect));
        registry.register(PreventNextColorDamageToControllerEffect.class,
                (gd, entry, effect) -> resolvePreventNextColorDamageToController(gd, entry, (PreventNextColorDamageToControllerEffect) effect));
    }

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

    void resolvePreventNextDamage(GameData gameData, PreventNextDamageEffect prevent) {
        gameData.globalDamagePreventionShield += prevent.amount();

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to any permanent or player is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, prevent.amount());
    }

    void resolvePreventAllCombatDamage(GameData gameData) {
        gameData.preventAllCombatDamage = true;

        String logEntry = "All combat damage will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    void resolvePreventDamageFromColors(GameData gameData, PreventDamageFromColorsEffect effect) {
        gameData.preventDamageFromColors.addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " and " + b)
                .orElse("");
        String logEntry = "All damage from " + colorNames + " sources will be prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    void resolvePreventNextColorDamageToController(GameData gameData, StackEntry entry, PreventNextColorDamageToControllerEffect effect) {
        CardColor chosenColor = effect.chosenColor();
        if (chosenColor == null) return;

        UUID controllerId = entry.getControllerId();
        gameData.playerColorDamagePreventionCount
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(chosenColor, 1, Integer::sum);
    }
}

