package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a continuation by placing its effect on the stack as a reflexive ability. */
@Component
@RequiredArgsConstructor
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        QueueReflexiveAbilityEffect queueEffect = (QueueReflexiveAbilityEffect) effect;
        if (graveyardTargetingSupport.findTarget(List.of(queueEffect.effect())) != null
                || queueEffect.effect().targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(queueEffect.effect())));
            return;
        }
        if (beginTargetChoice(gameData, entry, queueEffect.effect(), queueEffect.optionalTarget(),
                queueEffect.useEventValueAsX())) {
            return;
        }
        int xValue = queueEffect.useEventValueAsX() ? entry.getEventValue() : 0;
        StackEntry reflexiveEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                new ArrayList<>(List.of(queueEffect.effect())),
                xValue,
                entry.getSourcePermanentId());
        if (queueEffect.useEventValueAsX()) {
            reflexiveEntry.setEventValue(entry.getEventValue());
        }
        gameData.stack.add(reflexiveEntry);
    }

    private boolean beginTargetChoice(GameData gameData, StackEntry entry, CardEffect effect,
                                      boolean optionalTarget, boolean useEventValueAsX) {
        TargetSpec targetSpec = effect.targetSpec();
        if (!targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && !targetSpec.admits(TargetPredicate.Kind.PLAYER)
                && !targetSpec.admits(TargetPredicate.Kind.SPELL)) {
            return false;
        }

        TargetPredicate predicate = targetSpec.targetPredicate();
        int xValue = useEventValueAsX ? entry.getEventValue() : entry.getXValue();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withXValue(xValue);

        List<UUID> validPermanentIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (targetPredicateEvaluationService.matchesPermanent(predicate, permanent, filterContext)) {
                        validPermanentIds.add(permanent.getId());
                    }
                }
            }
        }

        if (targetSpec.admits(TargetPredicate.Kind.SPELL)) {
            for (StackEntry stackEntry : gameData.stack) {
                if (isSpellStackEntry(stackEntry)
                        && targetPredicateEvaluationService.matchesSpell(
                        predicate, stackEntry, entry.getControllerId(), entry.getSourcePermanentSnapshot(),
                        filterContext)) {
                    validPermanentIds.add(stackEntry.getCard().getId());
                }
            }
        }

        List<UUID> validPlayerIds = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (targetPredicateEvaluationService.matchesPlayer(
                        predicate, playerId, entry.getControllerId(), gameData)) {
                    validPlayerIds.add(playerId);
                }
            }
        }
        if (optionalTarget && !validPlayerIds.contains(entry.getControllerId())) {
            validPlayerIds.add(entry.getControllerId());
        }

        if (validPermanentIds.isEmpty() && validPlayerIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s reflexive ability has no valid targets."));
            return true;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(effect), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), entry.getEventValue(), xValue,
                optionalTarget));
        playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentIds,
                validPlayerIds, entry.getCard().getName() + "'s reflexive ability - Choose a target.");
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s reflexive ability - choose a target."));
        return true;
    }

    private boolean isSpellStackEntry(StackEntry entry) {
        return switch (entry.getEntryType()) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}
