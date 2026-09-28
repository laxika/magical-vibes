package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Fandaniel's per-opponent nontoken-creature sacrifice choice in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffectHandler
        implements NormalEffectHandlerBean {

    public static final PermanentPredicate NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTokenPredicate())));

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final LoseLifeEffectHandler loseLifeEffectHandler;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeEffect = (EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        for (UUID opponentId : AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                .apnapOpponents(gameData, controllerId)) {
            if (hasLegalSacrifice(gameData, entry, opponentId)) {
                gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                        entry.getCard(),
                        opponentId,
                        List.of(effect),
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
                        controllerId,
                        null,
                        0));
            } else {
                applyLifeLoss(gameData, entry, opponentId, sacrificeEffect.lifeLoss());
            }
        }
    }

    public boolean hasLegalSacrifice(GameData gameData, PendingMayAbility ability) {
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        return hasLegalSacrifice(gameData, ability.controllerId(), sourceControllerId,
                ability.sourceCard(), ability.sourcePermanentId(), ability.sourcePermanentSnapshot());
    }

    private boolean hasLegalSacrifice(GameData gameData, StackEntry entry, UUID playerId) {
        return hasLegalSacrifice(gameData, playerId, entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot());
    }

    private boolean hasLegalSacrifice(GameData gameData, UUID playerId, UUID sourceControllerId,
            Card sourceCard, UUID sourcePermanentId, Permanent sourcePermanentSnapshot) {
        if (sourceControllerId == null
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return false;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(sourceControllerId)
                .withSourcePermanentId(sourcePermanentId)
                .withSourcePermanentSnapshot(sourcePermanentSnapshot);
        return !destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                permanent, NONTOKEN_CREATURE, context)).isEmpty();
    }

    public UUID sourceControllerId(GameData gameData, PendingMayAbility ability) {
        if (ability.sourceControllerId() != null) {
            return ability.sourceControllerId();
        }
        UUID currentController = ability.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, ability.sourcePermanentId());
        return currentController != null ? currentController : ability.controllerId();
    }

    public void applyLifeLoss(GameData gameData, PendingMayAbility ability, DynamicAmount amount) {
        UUID sourceControllerId = sourceControllerId(gameData, ability);
        StackEntry contextEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                List.of(),
                ability.controllerId(),
                ability.sourcePermanentId());
        contextEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        applyLifeLoss(gameData, contextEntry, ability.controllerId(), amount);
    }

    private void applyLifeLoss(GameData gameData, StackEntry contextEntry, UUID playerId,
            DynamicAmount amount) {
        var lifeLoss = new LoseLifeEffect(amount, LoseLifeRecipient.TARGET_PLAYER);
        StackEntry lifeLossEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                contextEntry.getCard(),
                contextEntry.getControllerId(),
                contextEntry.getCard().getName() + "'s ability",
                List.of(lifeLoss),
                playerId,
                contextEntry.getSourcePermanentId());
        lifeLossEntry.setSourcePermanentSnapshot(contextEntry.getSourcePermanentSnapshot());
        loseLifeEffectHandler.resolve(gameData, lifeLossEntry, lifeLoss);
    }
}
