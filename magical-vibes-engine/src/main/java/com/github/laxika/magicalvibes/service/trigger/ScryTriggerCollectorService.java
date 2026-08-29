package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScryTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_CONTROLLER_SCRIES)
    private boolean handleSequenceOnScry(TriggerMatchContext match, SequenceEffect trigger, TriggerContext ctx) {
        return enqueueOnScry(match, trigger, ctx);
    }

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_CONTROLLER_SCRIES)
    private boolean handleMayPayOnScry(TriggerMatchContext match, MayPayManaEffect trigger, TriggerContext ctx) {
        return enqueueOnScry(match, trigger, ctx);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_SCRIES)
    private boolean handleSingleEffectOnScry(TriggerMatchContext match, CardEffect trigger, TriggerContext ctx) {
        return enqueueOnScry(match, trigger, ctx);
    }

    private boolean enqueueOnScry(TriggerMatchContext match, CardEffect trigger, TriggerContext ctx) {
        TriggerContext.Scry scry = (TriggerContext.Scry) ctx;
        Card sourceCard = match.permanent().getCard();
        TargetSpec targetSpec = trigger.targetSpec();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                || targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    new ArrayList<>(List.of(trigger)),
                    !targetSpec.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilter(targetSpec),
                    0,
                    match.permanent().getId()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    match.permanent().getId());
            entry.setEventValue(scry.bottomedCardCount());
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on scry", match.gameData().id, sourceCard.getName());
        return true;
    }

    private TargetFilter targetFilter(TargetSpec targetSpec) {
        TargetPredicate predicate = targetSpec.targetPredicate();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            PermanentPredicate permanentPredicate = predicate.permanentRestriction().orElseThrow();
            PlayerPredicate playerPredicate = predicate.leaf(TargetPredicate.Kind.PLAYER)
                    .map(leaf -> ((TargetPredicate.Players) leaf).inner())
                    .orElseThrow();
            return new AnyTargetPredicateTargetFilter(
                    permanentPredicate,
                    playerPredicate,
                    "Target must match the ability's targeting restriction");
        }
        if (targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            PlayerPredicate playerPredicate = predicate.leaf(TargetPredicate.Kind.PLAYER)
                    .map(leaf -> ((TargetPredicate.Players) leaf).inner())
                    .orElseThrow();
            return new PlayerPredicateTargetFilter(
                    playerPredicate,
                    "Target must match the ability's targeting restriction");
        }
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            PermanentPredicate permanentPredicate = predicate.permanentRestriction().orElseThrow();
            return new PermanentPredicateTargetFilter(
                    permanentPredicate,
                    "Target must match the ability's targeting restriction");
        }
        return null;
    }
}
