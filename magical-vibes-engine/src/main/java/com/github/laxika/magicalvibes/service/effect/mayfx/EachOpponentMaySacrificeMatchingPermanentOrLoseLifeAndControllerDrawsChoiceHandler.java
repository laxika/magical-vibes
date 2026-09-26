package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DrawCardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificePermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one opponent's choice for Braids's sacrifice-or-life-loss-and-draw clause. */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsChoiceHandler
        implements MayEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DrawCardEffectHandler drawCardEffectHandler;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var braidsEffect = (EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect)
                ability.effects().getFirst();
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        if (accepted && hasLegalSacrifice(gameData, ability, sourceControllerId)) {
            var sacrifice = new SacrificePermanentsEffect(
                    1,
                    new PermanentSharesCardTypeWithSourcePermanentPredicate(),
                    SacrificeRecipient.TARGET_PLAYER);
            var sacrificeEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    sourceControllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(sacrifice)),
                    ability.controllerId(),
                    (UUID) null);
            sacrificeEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            sacrificePermanentsEffectHandler.resolve(gameData, sacrificeEntry, sacrifice);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        lifeSupport.applyLifeLoss(gameData, ability.controllerId(), braidsEffect.lifeLoss(),
                ability.sourceCard().getName());
        drawCardEffectHandler.resolve(gameData, drawEntry(ability, sourceControllerId), new DrawCardEffect(1));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean hasLegalSacrifice(GameData gameData, PendingMayAbility ability,
            UUID sourceControllerId) {
        if (sourceControllerId == null
                || !gameQueryService.canEffectCauseSacrifice(
                        gameData, ability.controllerId(), sourceControllerId)
                || ability.sourcePermanentSnapshot() == null) {
            return false;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(ability.sourceCard().getId())
                .withSourceControllerId(sourceControllerId)
                .withSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        var filter = new PermanentSharesCardTypeWithSourcePermanentPredicate();
        return !destructionSupport.collectPermanentIds(gameData, ability.controllerId(),
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context))
                .isEmpty();
    }

    private StackEntry drawEntry(PendingMayAbility ability, UUID sourceControllerId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DrawCardEffect(1))),
                (UUID) null,
                (UUID) null);
        entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        return entry;
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
}
