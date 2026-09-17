package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the random-opponent sacrifice-or-damage choice. */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentUnlessSacrificeEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final InputCompletionService inputCompletionService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentUnlessSacrificeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomOpponentUnlessSacrificeEffect) effect;
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .filter(gameData.playerIds::contains)
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        UUID opponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        SacrificePermanentsEffect sacrifice = sacrificeEffect(e);
        StackEntry sacrificeEntry = syntheticEntry(entry, sacrifice, opponentId);
        if (!sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                gameData, sacrificeEntry, sacrifice, opponentId)) {
            dealDamage(gameData, entry, e, opponentId);
            return;
        }

        gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                entry.getCard(), opponentId,
                List.of(e),
                "Sacrifice a nontoken creature?",
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
                entry.getControllerId(),
                null,
                entry.getEventValue()));
    }

    public void resolveChoice(GameData gameData, PendingMayAbility ability,
            boolean accepted, DealDamageToRandomOpponentUnlessSacrificeEffect effect) {
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        SacrificePermanentsEffect sacrifice = sacrificeEffect(effect);
        StackEntry sacrificeEntry = syntheticEntry(ability, sacrifice, sourceControllerId, ability.controllerId());
        if (accepted && sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                gameData, sacrificeEntry, sacrifice, ability.controllerId())) {
            sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry, sacrifice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        dealDamage(gameData, sacrificeEntry, effect, ability.controllerId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private SacrificePermanentsEffect sacrificeEffect(DealDamageToRandomOpponentUnlessSacrificeEffect effect) {
        return new SacrificePermanentsEffect(1, effect.sacrificeFilter(), SacrificeRecipient.TARGET_PLAYER);
    }

    private void dealDamage(GameData gameData, StackEntry sourceEntry,
            DealDamageToRandomOpponentUnlessSacrificeEffect effect, UUID opponentId) {
        var damage = new DealDamageToPlayersEffect(effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                opponentId,
                sourceEntry.getSourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(sourceEntry.getSourcePermanentSnapshot());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    private StackEntry syntheticEntry(StackEntry sourceEntry, CardEffect effect, UUID targetId) {
        StackEntry result = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                targetId,
                sourceEntry.getSourcePermanentId());
        result.setSourcePermanentSnapshot(sourceEntry.getSourcePermanentSnapshot());
        return result;
    }

    private StackEntry syntheticEntry(PendingMayAbility ability, CardEffect effect,
            UUID sourceControllerId, UUID targetId) {
        StackEntry result = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                targetId,
                ability.sourcePermanentId());
        result.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        return result;
    }
}
