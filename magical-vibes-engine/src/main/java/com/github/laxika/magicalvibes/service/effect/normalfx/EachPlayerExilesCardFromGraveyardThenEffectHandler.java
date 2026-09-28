package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.EachPlayerExilesCardFromGraveyardContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Augusta's each-player graveyard exile and its nonland-count reflexive trigger. */
@Component
@RequiredArgsConstructor
public class EachPlayerExilesCardFromGraveyardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesCardFromGraveyardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (EachPlayerExilesCardFromGraveyardThenEffect) effect;
        var operation = gameData.graveyardTargetOperation;
        var context = operation.eachPlayerExilesCardFromGraveyard;
        if (context == null) {
            operation.entryType = null;
            operation.xValue = 0;
            operation.anyNumber = false;
            context = new EachPlayerExilesCardFromGraveyardContext(
                    entry.getControllerId(), entry.getSourcePermanentId(), exileThen.thenEffect(),
                    apnapOrder(gameData), null, 0);
            operation.eachPlayerExilesCardFromGraveyard = context;
        }

        List<UUID> remainingPlayerIds = new ArrayList<>(context.remainingPlayerIds());
        int nonlandCardsExiled = context.nonlandCardsExiled();
        while (!remainingPlayerIds.isEmpty()) {
            UUID playerId = remainingPlayerIds.removeFirst();
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            if (graveyard.size() > 1) {
                operation.eachPlayerExilesCardFromGraveyard = new EachPlayerExilesCardFromGraveyardContext(
                        context.controllerId(), context.sourcePermanentId(), context.thenEffect(),
                        remainingPlayerIds, playerId, nonlandCardsExiled);
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginMultiGraveyardChoice(
                        gameData, playerId, new ArrayList<>(graveyard), 1, 1,
                        entry.getCard().getName() + " — Choose a card to exile from your graveyard.");
                return;
            }

            Card card = graveyard.getFirst();
            exileCard(gameData, playerId, card);
            if (!card.hasType(CardType.LAND)) {
                nonlandCardsExiled++;
            }
            context = new EachPlayerExilesCardFromGraveyardContext(
                    context.controllerId(), context.sourcePermanentId(), context.thenEffect(),
                    remainingPlayerIds, null, nonlandCardsExiled);
            operation.eachPlayerExilesCardFromGraveyard = context;
        }

        operation.eachPlayerExilesCardFromGraveyard = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (nonlandCardsExiled == 0) {
            return;
        }

        entry.setEventValue(nonlandCardsExiled);
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Each-player graveyard exile effect is not on the stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(context.thenEffect(), false, true)));
    }

    private void exileCard(GameData gameData, UUID playerId, Card card) {
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        exileService.exileCard(gameData, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " exiles ", card, " from their graveyard."));
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = order.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return order;
        }
        List<UUID> rotated = new ArrayList<>(order.size());
        rotated.addAll(order.subList(activeIndex, order.size()));
        rotated.addAll(order.subList(0, activeIndex));
        return rotated;
    }
}
