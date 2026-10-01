package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPlanarModeEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves plane choices in APNAP order and stores them on the live face-up plane. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesPlanarModeEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesPlanarModeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var planarEffect = (EachPlayerChoosesPlanarModeEffect) effect;
        PlanarObject source = entry.getSourcePlanarObject();
        if (source == null) return;
        if (planarEffect.firstUpkeepOnly() && !isFirstUpkeep(gameData)) return;

        PlanarObject liveSource = findPlanarObject(gameData, source.getId());
        if (liveSource == null) return;
        if (planarEffect.switchExistingModes()) {
            switchModes(liveSource, planarEffect.modes());
            return;
        }

        beginNextChoice(gameData, planarEffect, source.getId(), apnapPlayers(gameData),
                entry.getCard() == null ? source.getCard().getName() : entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, String label,
                               ChoiceContext.EachPlayerChoosesPlanarModeChoice context,
                               UUID choosingPlayerId) {
        if (!context.effect().modes().contains(label)) {
            throw new IllegalArgumentException("Invalid planar mode choice: " + label);
        }
        PlanarObject source = findPlanarObject(gameData, context.planarObjectId());
        if (source != null) {
            source.getChosenModeByPlayer().put(choosingPlayerId, label);
        }
        beginNextChoice(gameData, context.effect(), context.planarObjectId(),
                context.remainingPlayerIds(), context.sourceName());
    }

    private void beginNextChoice(GameData gameData, EachPlayerChoosesPlanarModeEffect effect,
                                 UUID planarObjectId, List<UUID> remainingPlayerIds, String sourceName) {
        if (remainingPlayerIds.isEmpty() || findPlanarObject(gameData, planarObjectId) == null) return;

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> remaining = List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.EachPlayerChoosesPlanarModeChoice(
                        effect, planarObjectId, remaining, sourceName),
                effect.modes(), sourceName + " — choose one."));
    }

    private static void switchModes(PlanarObject source, List<String> modes) {
        if (modes.size() < 2) return;
        String first = modes.getFirst();
        String second = modes.get(1);
        source.getChosenModeByPlayer().replaceAll((playerId, mode) ->
                first.equals(mode) ? second : second.equals(mode) ? first : mode);
    }

    private static PlanarObject findPlanarObject(GameData gameData, UUID id) {
        if (gameData.planechase == null) return null;
        return gameData.planechase.faceUp.stream()
                .filter(planar -> planar.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static boolean isFirstUpkeep(GameData gameData) {
        return gameData.turnNumber == 1
                && gameData.activePlayerId != null
                && gameData.activePlayerId.equals(gameData.startingPlayerId);
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) return ordered;
        List<UUID> rotated = new ArrayList<>(ordered.size());
        rotated.addAll(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
