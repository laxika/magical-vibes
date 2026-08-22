package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.NoCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardNameMatchesCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collector for "whenever you play a card with the same name as one of the cards exiled with
 * this permanent" (Search the City). "Play a card" covers both halves, so the same effect is
 * collected from {@code ON_CONTROLLER_CASTS_SPELL} and {@code ON_CONTROLLER_PLAYS_LAND}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedCardNameTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @CollectsTrigger(value = PlayedCardNameMatchesCardExiledWithSourceTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerCastsSpell(TriggerMatchContext match,
            PlayedCardNameMatchesCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return collect(match, sc.spellCard());
    }

    @CollectsTrigger(value = PlayedCardNameMatchesCardExiledWithSourceTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleControllerPlaysLand(TriggerMatchContext match,
            PlayedCardNameMatchesCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        return collect(match, lp.landCard());
    }

    @CollectsTrigger(value = DrawCardForTargetPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_PLAYS_LAND)
    private boolean handleOpponentPlaysLandDraws(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setTargetId(lp.playingPlayerId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on an opponent playing a land for that player to draw",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = TriggeringCardConditionalEffect.class,
                    slot = EffectSlot.ON_CONTROLLER_PLAYS_LAND),
            @CollectsTrigger(value = TriggeringCardConditionalEffect.class,
                    slot = EffectSlot.ON_OPPONENT_PLAYS_LAND)
    })
    private boolean handleConditionalLandPlay(TriggerMatchContext match,
            TriggeringCardConditionalEffect conditional, TriggerContext ctx) {
        TriggerContext.LandPlayed landPlayed = (TriggerContext.LandPlayed) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), conditional.predicate(), null,
                match.gameData(), match.controllerId())) {
            return false;
        }

        CardEffect resolved = conditional.wrapped();
        if (resolved instanceof ConditionalEffect gate && gate.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), gate.condition(),
                ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }

        return enqueueLandPlayTrigger(match, resolved);
    }

    /**
     * Default for {@link EffectSlot#ON_CONTROLLER_PLAYS_LAND}: put bare effects on the stack
     * (e.g. Juju Bubble's {@code SacrificeSelfEffect}). Name-match triggers above take precedence
     * via exact-class registration.
     */
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleControllerPlaysLandDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (effect instanceof TriggeringCardConditionalEffect) {
            return false;
        }
        return enqueueLandPlayTrigger(match, effect);
    }

    /**
     * Default for {@link EffectSlot#ON_OPPONENT_PLAYS_LAND}: put bare effects on the stack
     * (e.g. Dirtcowl Wurm's {@code PutCountersOnSourceEffect}).
     */
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_PLAYS_LAND)
    private boolean handleOpponentPlaysLandDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (effect instanceof TriggeringCardConditionalEffect) {
            return false;
        }
        return enqueueLandPlayTrigger(match, effect);
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, CardEffect effect) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on playing a land",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * Fires only when a card exiled with this permanent shares the played card's name. The queued
     * ability is the optional "put one of those cards with that name into its owner's hand" followed
     * by the intervening-if "then if there are no cards exiled with this enchantment, sacrifice it.
     * If you do, take an extra turn after this one."
     */
    private boolean collect(TriggerMatchContext match, Card playedCard) {
        GameData gameData = match.gameData();
        UUID sourcePermanentId = match.permanent().getId();
        String playedName = playedCard.getName();

        boolean nameMatch = gameData.exiledCards.stream()
                .anyMatch(e -> sourcePermanentId.equals(e.sourcePermanentId())
                        && playedName.equals(e.card().getName()));
        if (!nameMatch) return false;

        Card sourceCard = match.permanent().getCard();
        List<CardEffect> effects = new ArrayList<>(List.of(
                new MayEffect(new PutCardExiledWithSourceIntoHandEffect(playedName),
                        "Put a card named " + playedName + " exiled with " + sourceCard.getName()
                                + " into your hand?"),
                new ConditionalEffect(new NoCardsExiledWithSource(),
                        SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1)))));

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                effects,
                null,
                sourcePermanentId);
        entry.setNonTargeting(true);
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on played card {} matching an exiled name",
                gameData.id, sourceCard.getName(), playedName);
        return true;
    }
}
