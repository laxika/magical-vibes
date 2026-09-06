package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToExileWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Sothera's choice-and-exile ability in APNAP order. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureToExileWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureToExileWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            sourcePermanentId = findSourcePermanentId(gameData, entry);
        }
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(), sourcePermanentId,
                apnapOpponents(gameData, entry.getControllerId()));
    }

    public void beginNextOpponent(GameData gameData, UUID controllerId, Card sourceCard,
            UUID sourcePermanentId, List<UUID> remainingOpponentIds) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, p -> true);
            if (creatureIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(opponentId) + " has no creatures to choose ("
                                + sourceCard.getName() + ")."));
                continue;
            }

            if (creatureIds.size() == 1) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                if (creature != null) {
                    exileChosen(gameData, creature, sourceCard, sourcePermanentId);
                }
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesCreatureToExileWithSource context =
                    new PermanentChoiceContext.EachOpponentChoosesCreatureToExileWithSource(
                            sourceCard, sourcePermanentId, controllerId, opponentId, List.copyOf(remaining));
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds, context,
                    sourceCard.getName() + " — choose a creature you control to exile.");
            return;
        }
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentChoosesCreatureToExileWithSource context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !context.choosingPlayerId().equals(
                gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is no longer a creature controlled by the choosing player");
        }

        exileChosen(gameData, chosen, context.sourceCard(), context.sourcePermanentId());
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(), context.sourcePermanentId(),
                context.remainingOpponentIds());
    }

    private void exileChosen(GameData gameData, Permanent creature, Card sourceCard, UUID sourcePermanentId) {
        if (sourcePermanentId != null) {
            exileSupport.exilePermanentAndTrackWithSource(gameData, creature, sourcePermanentId, sourceCard);
        } else {
            exileSupport.exilePermanentAndLog(gameData, creature, sourceCard.getName());
        }
    }

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
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
