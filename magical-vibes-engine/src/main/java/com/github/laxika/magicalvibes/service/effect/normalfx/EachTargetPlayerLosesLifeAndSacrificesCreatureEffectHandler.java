package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerLosesLifeAndSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachTargetPlayerLosesLifeAndSacrificesCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerLosesLifeAndSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachTargetPlayerLosesLifeAndSacrificesCreatureEffect loseLifeAndSacrifice =
                (EachTargetPlayerLosesLifeAndSacrificesCreatureEffect) effect;
        List<UUID> targetPlayerIds = entry.getTargetIds();
        if (targetPlayerIds == null || targetPlayerIds.isEmpty()) {
            if (entry.getTargetId() == null) {
                beginNextTarget(gameData, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                        List.of(), List.of());
                return;
            }
            targetPlayerIds = List.of(entry.getTargetId());
        }

        for (UUID targetPlayerId : targetPlayerIds) {
            if (gameData.playerIds.contains(targetPlayerId)) {
                lifeSupport.applyLifeLoss(gameData, targetPlayerId, loseLifeAndSacrifice.lifeLoss(),
                        entry.getCard().getName());
            }
        }

        beginNextTarget(gameData, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                apnapTargets(gameData, targetPlayerIds), List.of());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !context.choosingPlayerId().equals(
                gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is not a creature controlled by the choosing player");
        }

        List<UUID> chosenCreatureIds = new ArrayList<>(context.chosenCreatureIds());
        chosenCreatureIds.add(permanentId);
        beginNextTarget(gameData, context.sourceControllerId(), context.sourceCard(), context.sourcePermanentId(),
                context.remainingTargetPlayerIds(), chosenCreatureIds);
    }

    private void beginNextTarget(GameData gameData, UUID sourceControllerId, Card sourceCard,
                                 UUID sourcePermanentId, List<UUID> remainingTargetPlayerIds,
                                 List<UUID> chosenCreatureIds) {
        List<UUID> remaining = new ArrayList<>(remainingTargetPlayerIds);
        List<UUID> chosen = new ArrayList<>(chosenCreatureIds);

        while (!remaining.isEmpty()) {
            UUID targetPlayerId = remaining.removeFirst();
            if (!gameData.playerIds.contains(targetPlayerId)
                    || !gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, sourceControllerId)) {
                continue;
            }

            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, targetPlayerId,
                    ignored -> true);
            if (creatureIds.isEmpty()) {
                continue;
            }
            if (creatureIds.size() == 1) {
                chosen.add(creatureIds.getFirst());
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.EachTargetPlayerLosesLifeAndSacrificesCreature(
                            targetPlayerId, sourceControllerId, sourceCard, sourcePermanentId,
                            List.copyOf(remaining), List.copyOf(chosen)));
            playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                    sourceCard.getName() + " - Choose a creature to sacrifice.");
            return;
        }

        destructionSupport.performSimultaneousSacrifice(gameData, chosen);
    }

    private List<UUID> apnapTargets(GameData gameData, List<UUID> targetPlayerIds) {
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
                .filter(targetPlayerIds::contains)
                .toList();
    }
}
