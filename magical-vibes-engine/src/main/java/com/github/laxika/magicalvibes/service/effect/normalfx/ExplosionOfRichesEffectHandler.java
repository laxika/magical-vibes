package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.ExplosionOfRichesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Explosion of Riches and queues one random-target damage trigger per card drawn. */
@Component
@RequiredArgsConstructor
public class ExplosionOfRichesEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExplosionOfRichesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExplosionOfRichesEffect riches = (ExplosionOfRichesEffect) effect;
        UUID controllerId = riches.abilityControllerId() != null
                ? riches.abilityControllerId() : entry.getControllerId();
        int drawnCards = riches.drawnCards();

        if (riches.abilityControllerId() == null) {
            drawnCards += drawAndCount(gameData, controllerId);
        }

        List<UUID> opponents = riches.remainingOpponentIds() == null
                ? AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, controllerId)
                : new ArrayList<>(riches.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (opponents.isEmpty()) {
            queueDamageTriggers(gameData, entry.getCard(), controllerId, drawnCards);
            return;
        }

        promptNext(gameData, entry.getCard(), new ExplosionOfRichesEffect(
                opponents, controllerId, drawnCards, List.of()));
    }

    public void promptNext(GameData gameData, Card sourceCard, ExplosionOfRichesEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Draw a card? If you do, " + sourceCard.getName() + " deals 5 damage to a random opponent."));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               ExplosionOfRichesEffect effect, boolean accepted) {
        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        List<UUID> acceptedOpponents = new ArrayList<>(effect.acceptedOpponentIds());
        if (accepted) {
            acceptedOpponents.add(ability.controllerId());
        }
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new ExplosionOfRichesEffect(
                    remaining, effect.abilityControllerId(), effect.drawnCards(), acceptedOpponents));
            return;
        }

        int drawnCards = effect.drawnCards();
        for (UUID opponentId : acceptedOpponents) {
            drawnCards += drawAndCount(gameData, opponentId);
        }
        queueDamageTriggers(gameData, ability.sourceCard(), effect.abilityControllerId(), drawnCards);
    }

    private int drawAndCount(GameData gameData, UUID playerId) {
        int before = gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
        playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        return gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0) - before;
    }

    private void queueDamageTriggers(GameData gameData, Card sourceCard, UUID controllerId, int count) {
        List<UUID> opponents = AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                .apnapOpponents(gameData, controllerId);
        if (opponents.isEmpty()) {
            return;
        }

        for (int i = 0; i < count; i++) {
            UUID targetId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
            DealDamageToRandomOpponentEffect damage = DealDamageToRandomOpponentEffect.targeted(5);
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s reflexive ability",
                    new ArrayList<>(List.of(damage)),
                    targetId,
                    (UUID) null));
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
    }
}
