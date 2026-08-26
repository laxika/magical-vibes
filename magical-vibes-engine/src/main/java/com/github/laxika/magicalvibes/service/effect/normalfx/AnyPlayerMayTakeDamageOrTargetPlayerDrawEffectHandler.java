package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the sequential damage-or-draw choice used by Browbeat. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyPlayerMayTakeDamageOrTargetPlayerDrawEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DrawCardForTargetPlayerEffectHandler drawCardForTargetPlayerEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect) effect;
        UUID targetPlayerId = e.targetPlayerId() != null ? e.targetPlayerId() : entry.getTargetId();
        UUID sourceControllerId = e.sourceControllerId() != null
                ? e.sourceControllerId()
                : entry.getControllerId();
        List<UUID> players = apnapPlayers(gameData);
        if (players.isEmpty()) {
            drawCards(gameData, entry.getCard(), sourceControllerId, targetPlayerId, e);
            return;
        }

        promptNext(gameData, entry.getCard(), new AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect(
                e.damage(), e.drawCount(), targetPlayerId, List.copyOf(players), sourceControllerId));
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect effect) {
        if (effect.remainingPlayerIds() == null || effect.remainingPlayerIds().isEmpty()) {
            return;
        }

        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Have " + sourceCard.getName() + " deal " + effect.damage() + " damage to you?",
                effect.targetPlayerId(),
                (String) null,
                (UUID) null));
        log.info("Game {} - offering {} the {} take-damage choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    public void dealDamage(GameData gameData, PendingMayAbility ability,
                           AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect effect,
                           UUID playerId) {
        UUID sourceControllerId = effect.sourceControllerId() != null
                ? effect.sourceControllerId()
                : ability.controllerId();
        DealDamageToPlayersEffect damage =
                new DealDamageToPlayersEffect(effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                playerId,
                (UUID) null);
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    public void advance(GameData gameData, PendingMayAbility ability,
                        AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect effect,
                        UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));

        if (remaining.isEmpty()) {
            drawCards(gameData, ability.sourceCard(), effect.sourceControllerId(),
                    effect.targetPlayerId(), effect);
            return;
        }

        promptNext(gameData, ability.sourceCard(), new AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect(
                effect.damage(), effect.drawCount(), effect.targetPlayerId(),
                List.copyOf(remaining), effect.sourceControllerId()));
    }

    private void drawCards(GameData gameData, Card sourceCard, UUID sourceControllerId,
                           UUID targetPlayerId, AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect effect) {
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        DrawCardForTargetPlayerEffect draw = new DrawCardForTargetPlayerEffect(
                effect.drawCount(), false, true);
        StackEntry drawEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sourceControllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(draw)),
                targetPlayerId,
                (UUID) null);
        drawCardForTargetPlayerEffectHandler.resolve(gameData, drawEntry, draw);
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.size());
        rotated.addAll(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
