package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesColorThenExileOtherPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Selective Obliteration's APNAP color choices and selective exile. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesColorThenExileOtherPermanentsEffectHandler
        implements NormalEffectHandlerBean {

    private static final List<CardColor> COLORS = List.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesColorThenExileOtherPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> playerOrder = apnapPlayers(gameData);
        if (playerOrder.isEmpty()) {
            return;
        }

        UUID firstPlayerId = playerOrder.getFirst();
        beginColorChoice(gameData, firstPlayerId,
                new ChoiceContext.EachPlayerChoosesColorThenExileOtherPermanentsChoice(
                        playerOrder, Map.of()));
    }

    /** Completes one choice and returns whether all players have chosen. */
    public boolean completeChoice(GameData gameData,
                                  ChoiceContext.EachPlayerChoosesColorThenExileOtherPermanentsChoice context,
                                  CardColor chosenColor) {
        Map<UUID, CardColor> chosenColors = new LinkedHashMap<>(context.chosenColors());
        UUID choosingPlayerId = context.playerOrder().stream()
                .filter(playerId -> !chosenColors.containsKey(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No player is awaiting a color choice"));
        chosenColors.put(choosingPlayerId, chosenColor);

        UUID nextPlayerId = context.playerOrder().stream()
                .filter(playerId -> !chosenColors.containsKey(playerId))
                .findFirst()
                .orElse(null);
        if (nextPlayerId != null) {
            gameData.interaction.clearAwaitingInput();
            beginColorChoice(gameData, nextPlayerId,
                    new ChoiceContext.EachPlayerChoosesColorThenExileOtherPermanentsChoice(
                            context.playerOrder(), chosenColors));
            return false;
        }

        gameData.interaction.clearAwaitingInput();
        exilePermanents(gameData, chosenColors);
        return true;
    }

    private void beginColorChoice(GameData gameData, UUID playerId,
                                  ChoiceContext.EachPlayerChoosesColorThenExileOtherPermanentsChoice context) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, COLORS.stream().map(Enum::name).toList(),
                "Choose a color."));
        log.info("Game {} - Awaiting {} to choose a color for selective permanent exile",
                gameData.id, gameData.playerIdToName.get(playerId));
    }

    private void exilePermanents(GameData gameData, Map<UUID, CardColor> chosenColors) {
        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            CardColor chosenColor = chosenColors.get(controllerId);
            for (Permanent permanent : battlefield) {
                Set<CardColor> colors = gameQueryService.getEffectiveColors(gameData, permanent);
                boolean stays = colors.isEmpty() || (colors.size() == 1 && colors.contains(chosenColor));
                if (!stays) {
                    toExile.add(permanent);
                }
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, permanent);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by Selective Obliteration",
                        gameData.id, permanent.getCard().getName());
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry != null) {
            entry.setEventValue(toExile.size());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.size());
        rotated.addAll(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
