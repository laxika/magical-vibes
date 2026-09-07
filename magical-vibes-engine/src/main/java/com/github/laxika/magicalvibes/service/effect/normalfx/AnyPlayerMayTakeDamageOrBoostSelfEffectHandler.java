package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the Barbarian Bully damage-or-boost choice. */
@Slf4j
@Component
public class AnyPlayerMayTakeDamageOrBoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    public AnyPlayerMayTakeDamageOrBoostSelfEffectHandler(
            DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler,
            BoostSelfEffectHandler boostSelfEffectHandler) {
        this.dealDamageToPlayersEffectHandler = dealDamageToPlayersEffectHandler;
        this.boostSelfEffectHandler = boostSelfEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMayTakeDamageOrBoostSelfEffect) effect;
        List<UUID> players = e.remainingPlayerIds() == null
                ? apnapPlayers(gameData)
                : new ArrayList<>(e.remainingPlayerIds());
        players.removeIf(playerId -> !gameData.playerIds.contains(playerId));

        UUID abilityControllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        UUID sourcePermanentId = e.sourcePermanentId() != null
                ? e.sourcePermanentId()
                : entry.getSourcePermanentId();
        var stamped = new AnyPlayerMayTakeDamageOrBoostSelfEffect(
                e.damage(), e.powerBoost(), e.toughnessBoost(), List.copyOf(players),
                abilityControllerId, sourcePermanentId);

        if (players.isEmpty()) {
            boostSelf(gameData, entry.getCard(), stamped, entry.getSourcePermanentSnapshot());
            return;
        }

        promptNext(gameData, entry.getCard(), stamped, entry.getSourcePermanentSnapshot());
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayTakeDamageOrBoostSelfEffect effect,
                           Permanent sourcePermanentSnapshot) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Have " + sourceCard.getName() + " deal " + effect.damage()
                        + " damage to you? If no player does, it gets +" + effect.powerBoost()
                        + "/+" + effect.toughnessBoost() + " until end of turn.",
                effect.abilityControllerId(),
                null,
                effect.sourcePermanentId(),
                null,
                0,
                0,
                null,
                null,
                null,
                sourcePermanentSnapshot));
        log.info("Game {} - offering {} the {} damage-or-boost choice", gameData.id,
                gameData.playerIdToName.get(playerId), sourceCard.getName());
    }

    public List<UUID> remainingAfter(GameData gameData,
                                     AnyPlayerMayTakeDamageOrBoostSelfEffect effect,
                                     UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        return List.copyOf(remaining);
    }

    public void dealDamage(GameData gameData, PendingMayAbility ability,
                           AnyPlayerMayTakeDamageOrBoostSelfEffect effect, UUID playerId) {
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(
                effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                playerId,
                effect.sourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    public void boostSelf(GameData gameData, Card sourceCard,
                          AnyPlayerMayTakeDamageOrBoostSelfEffect effect,
                          Permanent sourcePermanentSnapshot) {
        BoostSelfEffect boost = new BoostSelfEffect(effect.powerBoost(), effect.toughnessBoost());
        StackEntry boostEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                sourceCard,
                effect.abilityControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(boost)),
                (UUID) null,
                effect.sourcePermanentId());
        boostEntry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        boostSelfEffectHandler.resolve(gameData, boostEntry, boost);
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
