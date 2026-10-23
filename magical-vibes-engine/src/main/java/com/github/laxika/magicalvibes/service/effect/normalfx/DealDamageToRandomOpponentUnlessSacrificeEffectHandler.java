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

    /** Whether the prompted opponent can still sacrifice a matching permanent. */
    public boolean hasLegalSacrifice(GameData gameData, PendingMayAbility ability,
            DealDamageToRandomOpponentUnlessSacrificeEffect effect) {
        SacrificePermanentsEffect sacrifice = sacrificeEffect(effect);
        return sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                gameData, sacrificeEntry(ability, sacrifice), sacrifice, ability.controllerId());
    }

    /**
     * The prompted opponent's sacrifice: a lone candidate is sacrificed at once, several open a
     * choice. Nothing changes before it.
     */
    public void sacrifice(GameData gameData, PendingMayAbility ability,
            DealDamageToRandomOpponentUnlessSacrificeEffect effect) {
        SacrificePermanentsEffect sacrifice = sacrificeEffect(effect);
        sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry(ability, sacrifice), sacrifice);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    /** The prompted opponent declined, or can no longer sacrifice: the source deals its damage to them. */
    public void dealDamageInstead(GameData gameData, PendingMayAbility ability,
            DealDamageToRandomOpponentUnlessSacrificeEffect effect) {
        dealDamage(gameData, sacrificeEntry(ability, sacrificeEffect(effect)), effect, ability.controllerId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private StackEntry sacrificeEntry(PendingMayAbility ability, SacrificePermanentsEffect sacrifice) {
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
        return syntheticEntry(ability, sacrifice, sourceControllerId, ability.controllerId());
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
