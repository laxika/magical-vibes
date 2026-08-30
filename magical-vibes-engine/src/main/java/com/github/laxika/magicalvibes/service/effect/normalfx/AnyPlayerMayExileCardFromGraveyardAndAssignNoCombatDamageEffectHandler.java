package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the Carrion Rats and Carrion Wurm any-player graveyard-exile choice. */
@Slf4j
@Component
public class AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect) effect;
        List<UUID> players = e.remainingPlayerIds() == null
                ? apnapPlayers(gameData)
                : new ArrayList<>(e.remainingPlayerIds());
        players.removeIf(playerId -> !gameData.playerIds.contains(playerId)
                || gameData.playerGraveyards.getOrDefault(playerId, List.of()).size() < e.cardsToExile());
        if (players.isEmpty()) {
            return;
        }

        UUID abilityControllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        UUID sourcePermanentId = e.sourcePermanentId() != null
                ? e.sourcePermanentId()
                : entry.getSourcePermanentId();
        promptNext(gameData, entry.getCard(), new AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect(
                e.cardsToExile(), List.copyOf(players), abilityControllerId, sourcePermanentId));
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Exile " + cardCountText(effect.cardsToExile()) + " from your graveyard? If you do, "
                        + sourceCard.getName()
                        + " assigns no combat damage this turn.",
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId()));
        log.info("Game {} - offering {} the {} graveyard-exile choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    public List<UUID> remainingAfter(GameData gameData,
                                     AnyPlayerMayExileCardFromGraveyardAndAssignNoCombatDamageEffect effect,
                                     UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id)
                || gameData.playerGraveyards.getOrDefault(id, List.of()).size() < effect.cardsToExile());
        return List.copyOf(remaining);
    }

    private static String cardCountText(int cardsToExile) {
        return cardsToExile == 1 ? "a card" : cardsToExile + " cards";
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
