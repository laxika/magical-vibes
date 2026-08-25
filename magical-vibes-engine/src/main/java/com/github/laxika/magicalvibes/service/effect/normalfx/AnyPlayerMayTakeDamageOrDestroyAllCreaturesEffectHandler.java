package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Breaking Point's damage-or-destruction choice. */
@Component
public class AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DestroyAllPermanentsEffectHandler destroyAllPermanentsEffectHandler;

    public AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffectHandler(
            DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler,
            DestroyAllPermanentsEffectHandler destroyAllPermanentsEffectHandler) {
        this.dealDamageToPlayersEffectHandler = dealDamageToPlayersEffectHandler;
        this.destroyAllPermanentsEffectHandler = destroyAllPermanentsEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect) effect;
        List<UUID> players = e.remainingPlayerIds() == null
                ? apnapPlayers(gameData)
                : new ArrayList<>(e.remainingPlayerIds());
        players.removeIf(playerId -> !gameData.playerIds.contains(playerId));

        UUID abilityControllerId = e.abilityControllerId() != null
                ? e.abilityControllerId()
                : entry.getControllerId();
        var stamped = new AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect(
                e.damage(), List.copyOf(players), abilityControllerId);

        if (players.isEmpty()) {
            destroyAllCreatures(gameData, entry.getCard(), abilityControllerId);
            return;
        }

        promptNext(gameData, entry.getCard(), stamped);
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                "Have " + sourceCard.getName() + " deal " + effect.damage()
                        + " damage to you? If no one does, destroy all creatures."));
    }

    public List<UUID> remainingAfter(GameData gameData,
                                     AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect effect,
                                     UUID playerId) {
        List<UUID> remaining = new ArrayList<>(effect.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        return List.copyOf(remaining);
    }

    public void dealDamage(GameData gameData, PendingMayAbility ability,
                           AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect effect,
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

    public void destroyAllCreatures(GameData gameData, Card sourceCard, UUID controllerId) {
        DestroyAllPermanentsEffect destroy = new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(), true);
        StackEntry destroyEntry = new StackEntry(
                StackEntryType.SORCERY_SPELL,
                sourceCard,
                controllerId,
                sourceCard.getName(),
                new ArrayList<>(List.of(destroy)),
                (UUID) null,
                (UUID) null);
        destroyAllPermanentsEffectHandler.resolve(gameData, destroyEntry, destroy);
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
