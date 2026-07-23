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
        EachPlayerTakesDamageUnlessPaysEffect e = (EachPlayerTakesDamageUnlessPaysEffect) effect;
        List<UUID> order = apnapOrder(gameData);
        gameData.eachPlayerDamageUnlessPaysRemaining.clear();
        if (order.size() > 1) {
            gameData.eachPlayerDamageUnlessPaysRemaining.addAll(order.subList(1, order.size()));
        }
        offerPay(gameData, entry, e, order.getFirst());
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
        StackEntry synthetic = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, ability.sourceCard(),
                sourceControllerId(gameData, ability),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                next, ability.sourcePermanentId());
        offerPay(gameData, synthetic, effect, next);
    }

    private void offerPay(GameData gameData, StackEntry entry,
            EachPlayerTakesDamageUnlessPaysEffect effect, UUID playerId) {
        // Always prompt — paying mana is a choice (same as ForcedCostOrElse / Force of Nature).
        // Accept-without-mana falls through to damage in the may-choice handler.
        String prompt = "Pay " + effect.manaCost() + "? If you don't, " + entry.getCard().getName()
                + " deals " + effect.damage() + " damage to you.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), playerId, List.of(effect), prompt,
                null, effect.manaCost(), entry.getSourcePermanentId()));
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

    private UUID sourceControllerId(GameData gameData, PendingMayAbility ability) {
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
