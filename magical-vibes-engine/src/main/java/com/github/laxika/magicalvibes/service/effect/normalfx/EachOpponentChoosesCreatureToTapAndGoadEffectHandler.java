package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToTapAndGoadEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Fell Beast's Shriek's per-opponent creature choices in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureToTapAndGoadEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureToTapAndGoadEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(),
                apnapOpponents(gameData, entry.getControllerId()), List.of());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentChoosesCreatureToTapAndGoad context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !context.choosingPlayerId().equals(
                gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is no longer a creature controlled by the choosing player");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(),
                context.remainingOpponentIds(), chosenPermanentIds);
    }

    private void beginNextOpponent(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> remainingOpponentIds, List<UUID> chosenPermanentIds) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, ignored -> true);
            if (creatureIds.isEmpty()) {
                continue;
            }

            if (creatureIds.size() == 1) {
                chosen.add(creatureIds.getFirst());
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesCreatureToTapAndGoad context =
                    new PermanentChoiceContext.EachOpponentChoosesCreatureToTapAndGoad(
                            sourceCard, controllerId, opponentId, List.copyOf(remaining), List.copyOf(chosen));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds, context,
                    sourceCard.getName() + " — choose a creature you control.");
            return;
        }

        tapAndGoad(gameData, controllerId, sourceCard, chosen);
    }

    private void tapAndGoad(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> chosenPermanentIds) {
        for (UUID permanentId : chosenPermanentIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
            if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
                continue;
            }

            tapUntapSupport.tapPermanent(gameData, creature, controllerId);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceCard.getName(), null, controllerId,
                    new GoadTargetCreatureUntilNextTurnEffect(), creature.getId(), null, null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }
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
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
