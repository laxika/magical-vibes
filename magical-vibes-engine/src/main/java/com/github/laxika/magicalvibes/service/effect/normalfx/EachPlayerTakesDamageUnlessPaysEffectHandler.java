package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerTakesDamageUnlessPaysEffect}: each player in APNAP order may pay
 * {@code manaCost} or take {@code damage}. Sequencing uses
 * {@link GameData#eachPlayerDamageUnlessPaysRemaining}.
 *
 * <p>{@link #offerToPlayers} exposes the same pay-or-take-damage queue to other effects that derive
 * a different payer list — including one with repeats, where a player is asked once per qualifying
 * object rather than once overall (Stench of Evil, one prompt per land destroyed).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerTakesDamageUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final com.github.laxika.magicalvibes.service.battlefield.GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerTakesDamageUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        offerToPlayers(gameData, entry, (EachPlayerTakesDamageUnlessPaysEffect) effect, apnapOrder(gameData));
    }

    /**
     * Queues one pay-or-take-damage prompt per entry in {@code payers} (order preserved, duplicates
     * meaningful) and offers the first. No-op when the list is empty.
     */
    public void offerToPlayers(GameData gameData, StackEntry entry,
            EachPlayerTakesDamageUnlessPaysEffect effect, List<UUID> payers) {
        gameData.eachPlayerDamageUnlessPaysRemaining.clear();
        if (payers.isEmpty()) {
            return;
        }
        if (payers.size() > 1) {
            gameData.eachPlayerDamageUnlessPaysRemaining.addAll(payers.subList(1, payers.size()));
        }
        offerPay(gameData, entry, effect, payers.getFirst(), entry.getControllerId());
    }

    /**
     * After a player's may-pay decision: deal damage if they declined/couldn't pay, then offer the
     * next remaining player (or finish). Called from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterPlayerDecision(GameData gameData, PendingMayAbility ability,
            EachPlayerTakesDamageUnlessPaysEffect effect, UUID playerId, boolean paid) {
        if (!paid) {
            dealDamageToPlayer(gameData, ability, effect, playerId);
        }
        offerNext(gameData, ability, effect);
    }

    private void offerNext(GameData gameData, PendingMayAbility ability,
            EachPlayerTakesDamageUnlessPaysEffect effect) {
        if (gameData.eachPlayerDamageUnlessPaysRemaining.isEmpty()) {
            return;
        }
        UUID next = gameData.eachPlayerDamageUnlessPaysRemaining.removeFirst();
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        StackEntry synthetic = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                next, ability.sourcePermanentId());
        offerPay(gameData, synthetic, effect, next, sourceControllerId);
    }

    private void offerPay(GameData gameData, StackEntry entry,
            EachPlayerTakesDamageUnlessPaysEffect effect, UUID playerId, UUID sourceControllerId) {
        // Always prompt — paying mana is a choice (same as ForcedCostOrElse / Force of Nature).
        // Accept-without-mana falls through to damage in the may-choice handler.
        String prompt = "Pay " + effect.manaCost() + "? If you don't, " + entry.getCard().getName()
                + " deals " + effect.damage() + " damage to you.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), playerId, List.of(effect), prompt,
                sourceControllerId, effect.manaCost(), entry.getSourcePermanentId()));
    }

    private void dealDamageToPlayer(GameData gameData, PendingMayAbility ability,
            EachPlayerTakesDamageUnlessPaysEffect effect, UUID playerId) {
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        DealDamageToPlayersEffect damage =
                new DealDamageToPlayersEffect(effect.damage(), DamageRecipient.TARGET_PLAYER);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(), sourceControllerId,
                ability.sourceCard().getName() + "'s ability", new ArrayList<>(List.of(damage)),
                playerId, ability.sourcePermanentId());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }

    /**
     * The damage source's controller: the id stamped onto the prompt when it was queued (a sorcery
     * has no source permanent to look up, and its controller may well be the player being asked),
     * falling back to the source permanent's controller and then to the other player.
     */
    private UUID sourceControllerId(GameData gameData, PendingMayAbility ability) {
        UUID stamped = ability.targetCardId();
        if (stamped != null && gameData.playerIds.contains(stamped)) {
            return stamped;
        }
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        if (sourceControllerId != null) {
            return sourceControllerId;
        }
        return gameData.orderedPlayerIds.stream()
                .filter(pid -> !pid.equals(ability.controllerId()))
                .findFirst()
                .orElse(ability.controllerId());
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
