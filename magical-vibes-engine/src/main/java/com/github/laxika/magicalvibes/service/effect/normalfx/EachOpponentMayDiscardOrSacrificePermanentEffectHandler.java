package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrSacrificePermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Queues Zoyowa's optional discard-or-sacrifice choice for each opponent in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayDiscardOrSacrificePermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayDiscardOrSacrificePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = (EachOpponentMayDiscardOrSacrificePermanentEffect) effect;
        UUID sourceControllerId = entry.getControllerId();
        for (UUID opponentId : apnapOpponents(gameData, sourceControllerId)) {
            if (hasDiscardOption(gameData, opponentId)) {
                gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                        entry.getCard(), opponentId,
                        List.of(choice),
                        "Discard a card?",
                        null,
                        null,
                        entry.getSourcePermanentId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        null,
                        entry.getSourcePermanentSnapshot(),
                        sourceControllerId,
                        null,
                        0));
            } else if (hasSacrificeOption(gameData, opponentId, sourceControllerId)) {
                gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                        entry.getCard(), opponentId,
                        List.of(choice.forSacrificeChoice()),
                        "Sacrifice a permanent?",
                        null,
                        null,
                        entry.getSourcePermanentId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        null,
                        entry.getSourcePermanentSnapshot(),
                        sourceControllerId,
                        null,
                        0));
            } else {
                dealDamage(gameData, entry, opponentId, choice.damageIfNeither());
            }
        }
    }

    private boolean hasDiscardOption(GameData gameData, UUID playerId) {
        List<?> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private boolean hasSacrificeOption(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return false;
        }
        return !destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)).isEmpty();
    }

    private void dealDamage(GameData gameData, StackEntry sourceEntry, UUID playerId, int amount) {
        var damage = new DealDamageToPlayersEffect(amount, DamageRecipient.TARGET_PLAYER);
        var damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                playerId,
                sourceEntry.getSourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(sourceEntry.getSourcePermanentSnapshot());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
