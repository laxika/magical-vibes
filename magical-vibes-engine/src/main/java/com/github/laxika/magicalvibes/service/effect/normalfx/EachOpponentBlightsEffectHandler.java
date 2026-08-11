package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentBlightsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a mandatory blight choice for each opponent in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentBlightsEffectHandler implements NormalEffectHandlerBean {

    private final BlightEffectHandler blightEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentBlightsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentBlightsEffect blight = (EachOpponentBlightsEffect) effect;
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                apnapOpponents(gameData, entry.getControllerId()), blight.count());
    }

    public void beginNextOpponent(GameData gameData, UUID sourceControllerId, Card sourceCard,
                                  UUID sourcePermanentId, List<UUID> remainingOpponentIds, int count) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, ignored -> true);
            if (creatureIds.isEmpty()) {
                continue;
            }

            BlightEffect blight = new BlightEffect(count, null);
            StackEntry sourceEntry = sourceEntry(sourceCard, sourceControllerId, sourcePermanentId);
            if (creatureIds.size() == 1) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                if (creature != null) {
                    blightEffectHandler.placeCountersAndQueueThen(gameData, sourceEntry, creature, blight);
                }
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.EachOpponentBlightsCreature(
                    opponentId, sourceControllerId, sourceCard, sourcePermanentId, List.copyOf(remaining), count));
            playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds,
                    sourceCard.getName() + " - Choose a creature to blight.");
            return;
        }
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachOpponentBlightsCreature context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null) {
            throw new IllegalStateException("Chosen creature no longer exists");
        }

        blightEffectHandler.placeCountersAndQueueThen(gameData,
                sourceEntry(context.sourceCard(), context.sourceControllerId(), context.sourcePermanentId()),
                creature, new BlightEffect(context.count(), null));
        beginNextOpponent(gameData, context.sourceControllerId(), context.sourceCard(),
                context.sourcePermanentId(), context.remainingOpponentIds(), context.count());
    }

    private StackEntry sourceEntry(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId) {
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sourceControllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(),
                0,
                sourcePermanentId);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }

        return rotated.stream()
                .filter(id -> !id.equals(controllerId))
                .toList();
    }
}
