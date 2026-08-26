package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a cast trigger that lets each player pay life to counter the triggering spell. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayPayLifeToCounterSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CounterSupport counterSupport;
    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayPayLifeToCounterSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTriggeringCardId();
        if (targetCardId == null) {
            return;
        }

        List<UUID> players = apnapPlayers(gameData);
        AnyPlayerMayPayLifeToCounterSpellEffect payLife = (AnyPlayerMayPayLifeToCounterSpellEffect) effect;
        players.removeIf(playerId -> !canPayLife(gameData, playerId, payLife.lifeCost()));
        if (players.isEmpty()) {
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyPlayerMayPayLifeToCounterSpellEffect(
                payLife.lifeCost(), List.copyOf(players), entry.getControllerId(), targetCardId));
    }

    public int lifeCost(GameData gameData, UUID playerId, DynamicAmount amount) {
        return amountEvaluationService.evaluate(gameData, amount, AmountContext.forCasting(playerId));
    }

    public boolean canPayLife(GameData gameData, UUID playerId, DynamicAmount amount) {
        int lifeCost = lifeCost(gameData, playerId, amount);
        return lifeCost > 0
                && gameData.playerIds.contains(playerId)
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayPayLifeToCounterSpellEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        int lifeCost = lifeCost(gameData, playerId, effect.lifeCost());
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Pay " + lifeCost + " life to counter " + sourceCard.getName() + "?",
                effect.targetCardId()));
    }

    public void payLife(GameData gameData, UUID playerId, DynamicAmount amount, Card sourceCard) {
        int lifeCost = lifeCost(gameData, playerId, amount);
        lifeSupport.applyLifePayment(gameData, playerId, lifeCost, sourceCard.getName());
    }

    public void counterSpell(GameData gameData, Card sourceCard,
                             AnyPlayerMayPayLifeToCounterSpellEffect effect) {
        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        if (sourceEntry == null) {
            sourceEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    effect.abilityControllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)));
        }
        StackEntry targetEntry = counterSupport.findCounterTargetExcludingSource(
                gameData, effect.targetCardId(), sourceEntry);
        if (targetEntry != null) {
            counterSupport.counterSpell(gameData, sourceEntry, targetEntry);
        }
    }

    public void advance(GameData gameData, Card sourceCard,
                        AnyPlayerMayPayLifeToCounterSpellEffect effect, UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !canPayLife(gameData, id, effect.lifeCost()));
        if (!remaining.isEmpty()) {
            promptNext(gameData, sourceCard, new AnyPlayerMayPayLifeToCounterSpellEffect(
                    effect.lifeCost(), List.copyOf(remaining), effect.abilityControllerId(),
                    effect.targetCardId()));
        }
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.size());
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            return rotated;
        }
        return ordered;
    }
}
