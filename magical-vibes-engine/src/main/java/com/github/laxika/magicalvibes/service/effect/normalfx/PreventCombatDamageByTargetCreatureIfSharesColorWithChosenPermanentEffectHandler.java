package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Guard Dogs' resolution-time permanent choice and reuses the combat prevention shield. */
@Component
@RequiredArgsConstructor
public class PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PreventDamageEffectHandler preventDamageEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventCombatDamageByTargetCreatureIfSharesColorWithChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> controlledPermanents = gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        if (controlledPermanents.isEmpty()) {
            return;
        }

        if (controlledPermanents.size() == 1) {
            entry.setChosenPermanentId(controlledPermanents.getFirst().getId());
            applyIfColorsOverlap(gameData, entry, controlledPermanents.getFirst().getId());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.GuardDogsPermanentChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                controlledPermanents.stream().map(Permanent::getId).toList(),
                "Choose a permanent you control.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Guard Dogs resolution is no longer pending");
        }

        UUID controllerId = entry.getControllerId();
        if (!controllerId.equals(gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Chosen permanent is not controlled by the ability's controller");
        }

        entry.setChosenPermanentId(chosenPermanentId);
        applyIfColorsOverlap(gameData, entry, chosenPermanentId);
    }

    private void applyIfColorsOverlap(GameData gameData, StackEntry entry, UUID chosenPermanentId) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        Permanent target = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (chosen == null || target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        if (gameQueryService.getEffectiveColors(gameData, chosen).stream()
                .noneMatch(gameQueryService.getEffectiveColors(gameData, target)::contains)) {
            return;
        }

        preventDamageEffectHandler.resolve(gameData, entry, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
