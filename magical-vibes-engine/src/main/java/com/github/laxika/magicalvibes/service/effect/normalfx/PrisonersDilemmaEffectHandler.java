package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PrisonersDilemmaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Prisoner's Dilemma's secret opponent choices and damage result. */
@Component
@RequiredArgsConstructor
public class PrisonersDilemmaEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PrisonersDilemmaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        beginNextChoice(gameData, opponents, new LinkedHashMap<>(), entry.getCard().getName(), entry);
    }

    public void completeChoice(GameData gameData, String choice,
                               ChoiceContext.PrisonersDilemmaChoice context) {
        if (!ChoiceContext.PrisonersDilemmaChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid Prisoner's Dilemma choice: " + choice);
        }

        Map<UUID, String> choices = new LinkedHashMap<>(context.choices());
        choices.put(context.currentPlayerId(), choice);
        beginNextChoice(gameData, context.remainingOpponentIds(), choices, context.sourceName(),
                gameData.pendingEffectResolutionEntry);
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingOpponentIds,
                                 Map<UUID, String> choices, String sourceName, StackEntry entry) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        remaining.removeIf(playerId -> !gameData.playerIds.contains(playerId));
        if (remaining.isEmpty()) {
            resolveChoices(gameData, entry, choices, sourceName);
            return;
        }

        UUID choosingPlayerId = remaining.removeFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.PrisonersDilemmaChoice(
                        choosingPlayerId, remaining, choices, sourceName),
                ChoiceContext.PrisonersDilemmaChoice.OPTIONS,
                sourceName + " — secretly choose silence or snitch."));
    }

    private void resolveChoices(GameData gameData, StackEntry entry,
                                Map<UUID, String> choices, String sourceName) {
        if (entry == null) {
            throw new IllegalStateException("Prisoner's Dilemma resolution is not resumable");
        }

        logChoices(gameData, choices, sourceName);
        boolean allSilence = !choices.isEmpty() && choices.values().stream()
                .allMatch(ChoiceContext.PrisonersDilemmaChoice.SILENCE::equals);
        boolean allSnitch = !choices.isEmpty() && choices.values().stream()
                .allMatch(ChoiceContext.PrisonersDilemmaChoice.SNITCH::equals);

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            for (Map.Entry<UUID, String> choice : choices.entrySet()) {
                if (allSnitch || allSilence
                        || ChoiceContext.PrisonersDilemmaChoice.SILENCE.equals(choice.getValue())) {
                    int damage = allSilence ? 4 : allSnitch ? 8 : 12;
                    damageSupport.dealDamageToPlayer(
                            gameData, entry, choice.getKey(),
                            gameQueryService.applyDamageMultiplier(gameData, damage, entry));
                }
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private void logChoices(GameData gameData, Map<UUID, String> choices, String sourceName) {
        StringBuilder reveal = new StringBuilder(sourceName).append(" reveals the choices: ");
        int index = 0;
        for (Map.Entry<UUID, String> choice : choices.entrySet()) {
            if (index++ > 0) {
                reveal.append(", ");
            }
            reveal.append(gameData.playerIdToName.get(choice.getKey()))
                    .append(" chose ").append(choice.getValue().toLowerCase());
        }
        gameLogService.append(gameData, GameLog.text(reveal.append('.').toString()));
    }
}
