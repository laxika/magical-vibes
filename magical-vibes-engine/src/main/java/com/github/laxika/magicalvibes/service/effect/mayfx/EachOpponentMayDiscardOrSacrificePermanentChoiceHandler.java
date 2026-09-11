package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificePermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves each opponent's optional discard-or-sacrifice choice for Zoyowa. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayDiscardOrSacrificePermanentChoiceHandler implements MayEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayDiscardOrSacrificePermanentEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachOpponentMayDiscardOrSacrificePermanentEffect effect = ability.effects().stream()
                .filter(EachOpponentMayDiscardOrSacrificePermanentEffect.class::isInstance)
                .map(EachOpponentMayDiscardOrSacrificePermanentEffect.class::cast)
                .findFirst()
                .orElseThrow();
        UUID sourceControllerId = sourceControllerId(gameData, ability);

        if (effect.choiceStage() == EachOpponentMayDiscardOrSacrificePermanentEffect.ChoiceStage.DISCARD) {
            if (accepted && hasDiscardOption(gameData, ability.controllerId())) {
                gameData.discardCausedByOpponent = true;
                playerInputService.beginDiscardChoice(gameData, ability.controllerId(), 1,
                        DiscardFollowUp.NONE);
                return;
            }

            if (hasSacrificeOption(gameData, ability.controllerId(), sourceControllerId,
                    effect.sacrificeFilter(), ability)) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        ability.sourceCard(),
                        ability.controllerId(),
                        List.of(effect.forSacrificeChoice()),
                        "Sacrifice a permanent?",
                        ability.targetCardId(),
                        ability.manaCost(),
                        ability.sourcePermanentId(),
                        ability.tapPermanentsCost(),
                        ability.lifeCost(),
                        ability.additionalLifeCost(),
                        ability.attackedTargetId(),
                        ability.activePlayerId(),
                        ability.choicePlayerId(),
                        ability.sourcePermanentSnapshot(),
                        sourceControllerId,
                        ability.triggeringCardId(),
                        ability.eventValue()));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            dealDamage(gameData, ability, effect.damageIfNeither(), sourceControllerId);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && hasSacrificeOption(gameData, ability.controllerId(), sourceControllerId,
                effect.sacrificeFilter(), ability)) {
            resolveSacrifice(gameData, ability, sourceControllerId, effect.sacrificeFilter());
            return;
        }

        dealDamage(gameData, ability, effect.damageIfNeither(), sourceControllerId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void resolveSacrifice(GameData gameData, PendingMayAbility ability,
            UUID sourceControllerId, PermanentPredicate sacrificeFilter) {
        var sacrifice = new SacrificePermanentsEffect(
                1, sacrificeFilter, SacrificeRecipient.TARGET_PLAYER);
        var sacrificeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(sacrifice)),
                ability.controllerId(),
                ability.sourcePermanentId());
        sacrificeEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry, sacrifice);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private boolean hasDiscardOption(GameData gameData, UUID playerId) {
        List<?> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private boolean hasSacrificeOption(GameData gameData, UUID playerId, UUID sourceControllerId,
            PermanentPredicate sacrificeFilter, PendingMayAbility ability) {
        if (sourceControllerId == null
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return false;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(ability.sourceCard().getId())
                .withSourceControllerId(sourceControllerId)
                .withSourcePermanentId(ability.sourcePermanentId())
                .withSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        return !destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                permanent, sacrificeFilter, context)).isEmpty();
    }

    private UUID sourceControllerId(GameData gameData, PendingMayAbility ability) {
        if (ability.sourceControllerId() != null) {
            return ability.sourceControllerId();
        }
        UUID currentController = ability.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        return currentController != null ? currentController : ability.controllerId();
    }

    private void dealDamage(GameData gameData, PendingMayAbility ability, DynamicAmount amount,
            UUID sourceControllerId) {
        var damage = new DealDamageToPlayersEffect(amount, DamageRecipient.TARGET_PLAYER);
        var damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(damage)),
                ability.controllerId(),
                ability.sourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damage);
    }
}
