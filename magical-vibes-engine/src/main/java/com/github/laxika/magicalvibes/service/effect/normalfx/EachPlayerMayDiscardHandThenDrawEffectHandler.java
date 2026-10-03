package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AcceptedPlayersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the per-player choices for a discard-hand-and-draw effect. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardHandThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardHandThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardHandThenDrawEffect discardEffect =
                (EachPlayerMayDiscardHandThenDrawEffect) effect;
        List<UUID> players = discardEffect.remainingPlayerIds().isEmpty()
                ? apnapPlayers(gameData)
                : discardEffect.remainingPlayerIds();
        if (!players.isEmpty()) {
            UUID sourceControllerId = discardEffect.sourceControllerId() != null
                    ? discardEffect.sourceControllerId() : entry.getControllerId();
            promptNext(gameData, entry.getCard(), new EachPlayerMayDiscardHandThenDrawEffect(
                    discardEffect.cardsToDraw(), sourceControllerId,
                    players, discardEffect.acceptedPlayerIds(), discardEffect.acceptedPlayersFollowUp()));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           EachPlayerMayDiscardHandThenDrawEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                sourceCard.getName() + " - You may discard your hand and draw "
                        + drawAmountDescription(gameData, effect, playerId) + " cards."));
    }

    public void resolveAcceptedPlayers(GameData gameData, StackEntry entry, Card sourceCard,
                                       UUID sourceControllerId, List<UUID> acceptedPlayerIds,
                                       DynamicAmount cardsToDraw,
                                       AcceptedPlayersAwareEffect followUp) {
        Permanent source = sourcePermanent(gameData, entry);

        for (UUID playerId : acceptedPlayerIds) {
            int drawAmount = evaluateDrawAmount(gameData, entry, source, playerId, cardsToDraw);
            discardHandEffectHandler.discardHand(gameData, playerId, sourceControllerId,
                    sourceCard.getName());
            playerInteractionSupport.applyDrawCards(gameData, playerId, drawAmount);
        }

        if (followUp != null && gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex,
                    List.of(followUp.withAcceptedPlayerIds(acceptedPlayerIds)));
        }
    }

    private int evaluateDrawAmount(GameData gameData, StackEntry entry, Permanent source,
                                   UUID playerId, DynamicAmount cardsToDraw) {
        AmountContext context = entry == null
                ? new AmountContext(playerId, source, null, 0, 0)
                : AmountContext.forStackEntry(entry, source).withControllerId(playerId);
        return Math.max(0, amountEvaluationService.evaluate(gameData, cardsToDraw, context));
    }

    private String drawAmountDescription(GameData gameData,
                                         EachPlayerMayDiscardHandThenDrawEffect effect,
                                         UUID playerId) {
        if (effect.cardsToDraw() instanceof Fixed fixed) {
            return Integer.toString(fixed.value());
        }
        return Integer.toString(evaluateDrawAmount(gameData, gameData.pendingEffectResolutionEntry,
                sourcePermanent(gameData, gameData.pendingEffectResolutionEntry), playerId,
                effect.cardsToDraw()));
    }

    private Permanent sourcePermanent(GameData gameData, StackEntry entry) {
        if (entry == null) {
            return null;
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source == null ? entry.getSourcePermanentSnapshot() : source;
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
            rotated.addAll(players.subList(0, activeIndex));
            return rotated;
        }
        return players;
    }
}
