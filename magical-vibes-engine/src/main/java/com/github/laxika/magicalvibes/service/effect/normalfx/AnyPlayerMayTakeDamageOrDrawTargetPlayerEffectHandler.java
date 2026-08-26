package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Browbeat's damage-or-draw choice. */
@Component
public class AnyPlayerMayTakeDamageOrDrawTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DrawCardForTargetPlayerEffectHandler drawCardForTargetPlayerEffectHandler;

    public AnyPlayerMayTakeDamageOrDrawTargetPlayerEffectHandler(
            DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler,
            DrawCardForTargetPlayerEffectHandler drawCardForTargetPlayerEffectHandler) {
        this.dealDamageToPlayersEffectHandler = dealDamageToPlayersEffectHandler;
        this.drawCardForTargetPlayerEffectHandler = drawCardForTargetPlayerEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(e);
        if (groupTargets != null && !entry.getTargetIds().isEmpty()) {
            if (groupTargets.isEmpty()) {
                return;
            }
            targetPlayerId = groupTargets.getFirst();
        }
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> players = e.remainingPlayerIds() == null
                ? apnapPlayers(gameData)
                : new ArrayList<>(e.remainingPlayerIds());
        players.removeIf(playerId -> !gameData.playerIds.contains(playerId));

        UUID abilityControllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        var stamped = new AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect(
                e.damage(), e.drawCount(), List.copyOf(players), abilityControllerId);

        if (players.isEmpty()) {
            drawTargetPlayer(gameData, entry.getCard(), abilityControllerId, targetPlayerId,
                    stamped.drawCount());
            return;
        }

        promptNext(gameData, entry.getCard(), stamped, targetPlayerId);
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect effect,
                           UUID targetPlayerId) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Have " + sourceCard.getName() + " deal " + effect.damage()
                        + " damage to you? If no one does, target player draws "
                        + effect.drawCount() + " cards.",
                targetPlayerId));
    }

    public List<UUID> remainingAfter(GameData gameData,
                                     AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect effect,
                                     UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        return List.copyOf(remaining);
    }

    public void dealDamage(GameData gameData, PendingMayAbility ability,
                           AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect effect,
                           UUID playerId) {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.SORCERY_SPELL,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName(),
                new ArrayList<>(List.of(damage)),
                playerId,
                (UUID) null);
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    public void drawTargetPlayer(GameData gameData, Card sourceCard, UUID controllerId,
                                 UUID targetPlayerId, int drawCount) {
        DrawCardForTargetPlayerEffect draw = new DrawCardForTargetPlayerEffect(drawCount);
        StackEntry drawEntry = new StackEntry(
                StackEntryType.SORCERY_SPELL,
                sourceCard,
                controllerId,
                sourceCard.getName(),
                new ArrayList<>(List.of(draw)),
                targetPlayerId,
                (UUID) null);
        drawCardForTargetPlayerEffectHandler.resolve(gameData, drawEntry, draw);
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
