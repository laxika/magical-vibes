package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.NoCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateFoodWhenPlayingCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardNameMatchesCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceDrawAndTransformTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithPlanarSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentSnapshotRequiredEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
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
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;

/**
 * Trigger collector for the land-play abilities ("whenever [a player] plays a land"): a filtered one
 * ({@link #collectLandPlayAbility}) is read as an ability, and the handlers bind the event data of the "play a card"
 * abilities, such as "whenever you play a card with the same name as one of the cards exiled with this permanent"
 * (Search the City). "Play a card" covers both halves, so the same effect is collected from
 * {@code ON_CONTROLLER_CASTS_SPELL} and {@code ON_CONTROLLER_PLAYS_LAND}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayedCardNameTriggerCollectorService {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @CollectsSpellAbilityTriggers({
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL),
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL)
    })
    private boolean handleControllerCastsExiledCard(TriggerMatchContext match,
            PlayedCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return sc.castZone() == Zone.EXILE
                && collectFollowUp(match, sc.exiledSourcePermanentId(), sc.castingPlayerId(),
                sc.spellCard(), trigger.followUpEffect());
    }

    @CollectsSpellAbilityTriggers({
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND),
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND)
    })
    private boolean handleControllerPlaysExiledCardLand(TriggerMatchContext match,
            PlayedCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        return lp.fromExile()
                && collectFollowUp(match, lp.exiledSourcePermanentId(), lp.playingPlayerId(),
                lp.landCard(), trigger.followUpEffect());
    }

    @CollectsSpellAbilityTriggers({
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithPlanarSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    })
    private boolean handleControllerCastsCardExiledWithPlanarSource(
            TriggerMatchContext match, PlayedCardExiledWithPlanarSourceTriggerEffect trigger,
            TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return match.sourcePlanarObject() != null
                && sc.castZone() == Zone.EXILE
                && match.sourcePlanarObject().getId().equals(sc.exiledSourcePermanentId())
                && enqueuePlanarFollowUp(match, sc.castingPlayerId(), sc.spellCard(), trigger.followUpEffect());
    }

    @CollectsSpellAbilityTriggers({
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithPlanarSourceTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    })
    private boolean handleControllerPlaysCardExiledWithPlanarSource(
            TriggerMatchContext match, PlayedCardExiledWithPlanarSourceTriggerEffect trigger,
            TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        return match.sourcePlanarObject() != null
                && lp.fromExile()
                && match.sourcePlanarObject().getId().equals(lp.exiledSourcePermanentId())
                && enqueuePlanarFollowUp(match, lp.playingPlayerId(), lp.landCard(), trigger.followUpEffect());
    }

    @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceDrawAndTransformTriggerEffect.class,
            watch = SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleAnyPlayerCastsExiledCard(TriggerMatchContext match,
            PlayedCardExiledWithSourceDrawAndTransformTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return sc.castZone() == Zone.EXILE
                && collectDrawAndTransform(match, sc.exiledSourcePermanentId(), sc.spellCard());
    }

    @CollectsSpellAbilityTriggers({
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceDrawAndTransformTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND),
            @CollectsSpellAbilityTrigger(value = PlayedCardExiledWithSourceDrawAndTransformTriggerEffect.class,
                    watch = SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND)
    })
    private boolean handlePlayerPlaysExiledCardLand(TriggerMatchContext match,
            PlayedCardExiledWithSourceDrawAndTransformTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        return lp.fromExile()
                && collectDrawAndTransform(match, lp.exiledSourcePermanentId(), lp.landCard());
    }

    @CollectsSpellAbilityTrigger(value = PlayedCardNameMatchesCardExiledWithSourceTriggerEffect.class,
            watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerCastsSpell(TriggerMatchContext match,
            PlayedCardNameMatchesCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return collect(match, sc.spellCard());
    }

    @CollectsSpellAbilityTrigger(value = PlayedCardNameMatchesCardExiledWithSourceTriggerEffect.class,
            watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleControllerPlaysLand(TriggerMatchContext match,
            PlayedCardNameMatchesCardExiledWithSourceTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed lp = (TriggerContext.LandPlayed) ctx;
        return collect(match, lp.landCard());
    }

    @CollectsSpellAbilityTrigger(value = DrawCardForTargetPlayerEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND)
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

    /**
     * Collects {@code ability}, whose land-play trigger {@code trigger} narrows the land played by a filter or by the
     * zone it was played from, for the land play {@code landPlayed}: the trigger's filters and the intervening "if"
     * (CR 603.4) are judged as the land is played, and the ability goes on the stack as one object (CR 603.3), with
     * "that player" the player who played the land. An intervening "if" is checked again as the ability resolves, and
     * so is a filter that reads the card exiled with the source ("if it shares a card type with the exiled card"). An
     * ability whose effects are bound to a target of the card, or target a permanent, chooses its targets as it
     * triggers (CR 603.3d). The ability's limit is judged by the caller.
     *
     * @return whether the ability triggered
     */
    public boolean collectLandPlayAbility(TriggerMatchContext match, TriggeredAbility ability, LandPlayTrigger trigger,
                                          TriggerContext.LandPlayed landPlayed) {
        if (trigger.playedFrom() != null && landPlayed.playZone() != trigger.playedFrom()
                || trigger.notPlayedFrom() != null && landPlayed.playZone() == trigger.notPlayedFrom()) {
            return false;
        }
        Card sourceCard = match.sourceCard();
        UUID sourceCardId = match.permanent() != null ? match.permanent().getOriginalCard().getId() : sourceCard.getId();
        CardPredicate filter = trigger.filter();
        if (filter != null && !predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), filter,
                sourceCardId, match.gameData(), match.controllerId())) {
            return false;
        }
        Condition interveningIf = ability.options().interveningIf();
        if (interveningIf != null && !conditionEvaluationService.isMet(match.gameData(), interveningIf,
                match.permanent() != null
                        ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                        : ConditionContext.forCard(sourceCard, match.controllerId()))) {
            return false;
        }
        boolean recheckFilter = filter instanceof CardSharesCardTypeWithImprintedCardPredicate imprint
                && imprint.requireImprintedCard();
        List<CardEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            if (recheckFilter) {
                effect = new ConditionalEffect(new TriggeringCardMatches(filter), effect);
            }
            effects.add(effect);
        }
        TargetFilter boundFilter = boundTargetFilter(sourceCard, ability.effects().stream()
                .map(EffectRegistration::effect).toList());
        boolean needsPermanentTarget = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        if (needsPermanentTarget || boundFilter != null) {
            boolean needsPlayerTarget = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), effects, needsPlayerTarget && !needsPermanentTarget,
                    boundFilter, 0, match.permanent() != null ? match.permanent().getId() : null));
            gameLogService.append(match.gameData(), GameLog.cardThen(sourceCard,
                    "'s triggered ability triggers — choose a target."));
            return true;
        }
        return enqueueLandPlayTrigger(match, effects, landPlayed.playingPlayerId(), landPlayed.landCard().getId());
    }

    @CollectsSpellAbilityTrigger(value = CreateFoodWhenPlayingCardFromTopOfLibraryEffect.class,
            watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleFoodWhenPlayingLandFromLibraryTop(TriggerMatchContext match,
            CreateFoodWhenPlayingCardFromTopOfLibraryEffect trigger, TriggerContext ctx) {
        TriggerContext.LandPlayed landPlayed = (TriggerContext.LandPlayed) ctx;
        if (landPlayed.playZone() != Zone.LIBRARY
                || !predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), trigger.filter(),
                match.permanent().getOriginalCard().getId(), match.gameData(), match.controllerId())
                || !match.gameData().oncePerTurnLibraryPlayPermissionsUsedThisTurn
                .contains(match.permanent().getId())) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(CreateTokenEffect.ofFoodToken(1))),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }

    /**
     * Default for {@link SpellAbilityWatch#ON_CONTROLLER_PLAYS_LAND}: put bare effects on the stack
     * (e.g. Juju Bubble's {@code SacrificeSelfEffect}). Name-match triggers above take precedence
     * via exact-class registration.
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_ANOTHER_LAND)
    private boolean handleControllerPlaysLandDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        return enqueueLandPlayTrigger(match, effect, ((TriggerContext.LandPlayed) ctx).playingPlayerId());
    }

    /**
     * Default for {@link SpellAbilityWatch#ON_OPPONENT_PLAYS_LAND}: put bare effects on the stack
     * (e.g. Dirtcowl Wurm's {@code PutCountersOnSourceEffect}).
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND)
    private boolean handleOpponentPlaysLandDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        return enqueueLandPlayTrigger(match, effect, ((TriggerContext.LandPlayed) ctx).playingPlayerId());
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, CardEffect effect,
            UUID playingPlayerId) {
        return enqueueLandPlayTrigger(match, List.of(effect), playingPlayerId);
    }

    /**
     * The filter of the target group the ability's effects are bound to, or {@code null} for none: a "whenever you
     * play a land from exile" ability declares its target as a target group of the card.
     */
    private static TargetFilter boundTargetFilter(Card sourceCard, List<CardEffect> effects) {
        return effects.stream()
                .mapToInt(sourceCard::getEffectTargetIndex)
                .filter(index -> index >= 0 && index < sourceCard.getTargetGroups().size())
                .mapToObj(index -> sourceCard.getTargetGroups().get(index).getFilter())
                .findFirst().orElse(null);
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, List<CardEffect> effects, UUID playingPlayerId) {
        return enqueueLandPlayTrigger(match, effects, playingPlayerId, null);
    }

    private boolean enqueueLandPlayTrigger(TriggerMatchContext match, List<CardEffect> effects,
                                           UUID playingPlayerId, UUID triggeringCardId) {
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                match.permanent() != null ? match.permanent().getId() : null);
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject());
        }
        entry.setTargetId(playingPlayerId);
        if (triggeringCardId != null) {
            entry.setTriggeringCardId(triggeringCardId);
        }
        entry.setNonTargeting(true);
        if (match.permanent() != null
                && effects.stream().anyMatch(SourcePermanentSnapshotRequiredEffect.class::isInstance)) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on playing a land",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    private boolean collectFollowUp(TriggerMatchContext match, UUID exiledSourcePermanentId,
                                    UUID playingPlayerId, Card playedCard, CardEffect followUpEffect) {
        if (!match.permanent().getId().equals(exiledSourcePermanentId)) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(followUpEffect)),
                null,
                match.permanent().getId());
        entry.setTargetId(playingPlayerId);
        entry.setNonTargeting(true);
        // Snapshot the source so a transform instruction can tell whether it has already transformed
        // since this trigger was put on the stack (CR 701.27f).
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is played from exile",
                match.gameData().id, sourceCard.getName(), playedCard.getName());
        return true;
    }

    private boolean enqueuePlanarFollowUp(TriggerMatchContext match, UUID playingPlayerId,
                                          Card playedCard, CardEffect followUpEffect) {
        Card sourceCard = match.sourceCard();
        if (sourceCard == null || match.sourcePlanarObject() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(followUpEffect)));
        UUID ownerId = playedCard.getOwnerId() != null ? playedCard.getOwnerId() : playingPlayerId;
        entry.setTargetId(ownerId);
        entry.setNonTargeting(true);
        entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is played from planar exile",
                match.gameData().id, sourceCard.getName(), playedCard.getName());
        return true;
    }

    private boolean collectDrawAndTransform(TriggerMatchContext match, UUID exiledSourcePermanentId,
                                            Card playedCard) {
        if (!match.permanent().getId().equals(exiledSourcePermanentId)) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(new DrawCardEffect(1), new TransformToBackFaceEffect())),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is played from exile",
                match.gameData().id, sourceCard.getName(), playedCard.getName());
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
                new MayEffect(new PutCardExiledWithSourceIntoHandEffect(playedName, null, true),
                        "Put a card named " + playedName + " exiled with " + sourceCard.getName()
                                + " into your hand?"),
                new ConditionalEffect(new NoCardsExiledWithSource(),
                        new ConditionalEffect(new SourceIsOnBattlefield(),
                                SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1))))));

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
