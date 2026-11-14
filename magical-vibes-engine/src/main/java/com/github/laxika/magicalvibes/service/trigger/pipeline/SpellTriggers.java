package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.SpellManaValueEqualsSourceCounters;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByWatchedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCouldTargetAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.LandPlayTrigger;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.TriggerMatchContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Triggers the abilities of a {@link SpellEvent}: a spell cast, copied, countered or countered or fizzled, a spell's
 * card advantage, a land played, an ability activated and a mana ability resolved. The abilities are read as the layers
 * leave them ({@link TriggerIndex#permanentAbilities} for a permanent or a face-up scheme, a printed one through the
 * permanent's text changes, CR 612.1), each judged by its trigger condition (its spell, land or activation filter and
 * its event condition, CR 603.2) and handed whole to the stack door with the record of the event; the door judges its
 * intervening "if" (CR 603.4) and limit and chooses its targets as it goes on the stack (CR 603.3d).
 *
 * <p>A cast triggers, in this order: "whenever a player casts a spell" (permanents, face-up schemes, the abilities a
 * resolution registered, face-up planes), "whenever you cast a spell" (the caster's permanents, a departed mana
 * source's "when you spend this mana" rider, face-up schemes, planes, registered abilities), "whenever an opponent
 * casts a spell" (permanents, face-up schemes, suspended cards), the emblems', the caster's graveyard, hand, exile and
 * command zone cards', and the spell's own "when you cast this spell" abilities. The engine's keyword and delayed
 * abilities of the cast ({@code TriggerCollectionService.checkSpellCastTriggers}) go on the stack around them; their
 * controllers order them all the same (CR 603.3b).
 */
@Slf4j
@Service
public class SpellTriggers {

    private static final Trigger PLAYER_CASTS = Triggers.playerCastsSpell();
    private static final Trigger YOU_CAST = Triggers.youCastSpell();
    private static final Trigger OPPONENT_CASTS = Triggers.opponentCastsSpell();
    private static final Trigger YOU_CAST_THIS = Triggers.youCastThisSpell();
    private static final Trigger YOU_COPY = Triggers.youCopySpell();
    private static final Trigger OPPONENT_COPIES = Triggers.opponentCopiesSpell();
    private static final Trigger YOU_COUNTER = Triggers.youCounterSpell();
    private static final Trigger YOUR_SPELL_COUNTERED = Triggers.yourSpellCountered();
    private static final Trigger THIS_COUNTERED_OR_FIZZLES = Triggers.thisSpellCounteredOrFizzles();
    private static final Trigger CARD_ADVANTAGE = Triggers.yourSpellCausesYouToGainCardAdvantage();
    private static final Trigger YOU_PLAY_LAND = Triggers.youPlayLand();
    private static final Trigger YOU_PLAY_ANOTHER_LAND = Triggers.youPlayAnotherLand();
    private static final Trigger OPPONENT_PLAYS_LAND = Triggers.opponentPlaysLand();
    private static final Trigger THIS_MANA_ABILITY_RESOLVES = Triggers.thisManaAbilityResolves();

    private final TriggerIndex triggerIndex;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TargetLegalityService targetLegalityService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ValidTargetService validTargetService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    public SpellTriggers(TriggerIndex triggerIndex, @Lazy GameQueryService gameQueryService,
                         @Lazy PredicateEvaluationService predicateEvaluationService,
                         @Lazy GameLogService gameLogService, @Lazy TargetLegalityService targetLegalityService,
                         @Lazy ConditionEvaluationService conditionEvaluationService,
                         @Lazy ValidTargetService validTargetService,
                         @Lazy GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                         @Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.triggerIndex = triggerIndex;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.targetLegalityService = targetLegalityService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.validTargetService = validTargetService;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.triggeredAbilityStackService = triggeredAbilityStackService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, SpellEvent event) {
        switch (event) {
            case SpellEvent.Cast cast -> cast(gameData, cast);
            case SpellEvent.Copied copied -> copied(gameData, copied);
            case SpellEvent.Countered countered -> countered(gameData, countered);
            case SpellEvent.SpellCountered countered -> spellCountered(gameData, countered);
            case SpellEvent.CounteredOrFizzled countered -> counteredOrFizzled(gameData, countered);
            case SpellEvent.CardAdvantage advantage -> cardAdvantage(gameData, advantage);
            case SpellEvent.LandPlayed landPlayed -> landPlayed(gameData, landPlayed);
            case SpellEvent.Activated activated -> activated(gameData, activated);
            case SpellEvent.ManaAbilityResolved resolved -> manaAbilityResolved(gameData, resolved);
        }
    }

    /** The abilities a spell becoming cast triggers (CR 601.2i), in the order the class describes. */
    private void cast(GameData gameData, SpellEvent.Cast event) {
        Card spellCard = event.spellCard();
        UUID castingPlayerId = event.castingPlayerId();
        Zone castZone = event.castZone();
        gameData.forEachPermanent((playerId, perm) -> dispatchSpellWatch(gameData, perm, playerId, PLAYER_CASTS,
                event));
        dispatchFaceUpSchemes(gameData, PLAYER_CASTS, event, ownerId -> true);
        collectTemporaryGlobalSpellCastTriggers(gameData, spellCard, castingPlayerId);
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), gameData.planechase.controllerId,
                        PLAYER_CASTS, Zone.BATTLEFIELD, event, object);
            }
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) {
                dispatchSpellWatch(gameData, perm, playerId, YOU_CAST, event);
            }
        });
        dispatchDepartedManaSourceSpellCastTriggers(gameData, spellCard, castingPlayerId, event);
        dispatchFaceUpSchemes(gameData, YOU_CAST, event, castingPlayerId::equals);
        if (gameData.planechase != null && castingPlayerId.equals(gameData.planechase.controllerId)) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), castingPlayerId, YOU_CAST, Zone.BATTLEFIELD,
                        event, object);
            }
        }
        collectTemporaryControllerSpellCastTriggers(gameData, event);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) {
                dispatchSpellWatch(gameData, perm, playerId, OPPONENT_CASTS, event);
            }
        });
        dispatchFaceUpSchemes(gameData, OPPONENT_CASTS, event, ownerId -> !ownerId.equals(castingPlayerId));
        dispatchSuspendedExiledCardSpellCastTriggers(gameData, castingPlayerId, event);

        // The emblems' cast abilities (Ajani, Jace, Telepath Unbound, Ral, Garruk, Caller of Beasts, Venser, ...).
        for (Emblem emblem : gameData.emblems) {
            collectEmblemCastAbilities(gameData, emblem, event);
        }

        // The spell as it is on the stack, its mana spent recorded as it became cast: the graveyard and hand abilities
        // below judge their intervening "if" on it and keep it as their triggering spell for the check as they
        // resolve.
        StackEntry castSpellEntry = findStackEntryForCard(gameData, spellCard.getId());
        // "Whenever you cast a spell" abilities of cards in the caster's graveyard (Lingering Phantom: "Whenever you
        // cast a historic spell, you may pay {B}. If you do, return this card from your graveyard to your hand.").
        for (Card card : new ArrayList<>(gameData.playerGraveyards.getOrDefault(castingPlayerId, List.of()))) {
            for (TriggeredAbility ability : triggerIndex.graveyardAbilities(gameData, card)) {
                SpellFilter filter = watchedSpellFilter(ability, YOU_CAST, Zone.GRAVEYARD, false);
                if (filter == null) continue;
                requireOnlyInterveningIf(ability, card);
                if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) continue;
                if (filter.stackEntry() != null && (castSpellEntry == null
                        || !targetLegalityService.matchesStackEntryPredicate(gameData, castSpellEntry,
                        filter.stackEntry(), castingPlayerId))) {
                    continue;
                }
                if (filter.nthThisTurn() > 0 && !isNthMatchingSpell(gameData, filter, castingPlayerId)) {
                    continue;
                }
                // The intervening "if" (CR 603.4: "if five or more mana was spent to cast it", Covercast) is judged
                // by the stack door on the spell, which the entry keeps as its triggering spell for the check as it
                // resolves. "If this card is in your graveyard" reads this graveyard incarnation from the entry
                // (CR 400.7), so the ability does nothing if the card has left and returned to the graveyard.
                long sourceEntryVersion = gameData.graveyardEntryVersion(card.getId());
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                        castingPlayerId, card.getName() + "'s ability",
                        ConditionContext.forCard(card, castingPlayerId).withTriggeringSpell(castSpellEntry),
                        entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setSourceGraveyardEntryVersion(sourceEntryVersion);
                            entry.setTriggeringSpell(castSpellEntry);
                        }));
            }
        }

        // Hand-resident cast abilities (Covercast: "Whenever you cast another instant or sorcery spell, if five or
        // more mana was spent to cast it, this card intensifies.").
        for (Card card : new ArrayList<>(gameData.playerHands.getOrDefault(castingPlayerId, List.of()))) {
            if (card.getId().equals(spellCard.getId())) continue;
            for (TriggeredAbility ability : triggerIndex.cardAbilities(gameData, card)) {
                SpellFilter filter = watchedSpellFilter(ability, YOU_CAST, Zone.HAND, false);
                if (filter == null) continue;
                requireOnlyInterveningIf(ability, card);
                if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) {
                    continue;
                }
                // The intervening "if" (Covercast's mana spent) is judged by the stack door now and as it resolves,
                // on the entry's triggering spell.
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                        castingPlayerId, card.getName() + "'s ability",
                        ConditionContext.forCard(card, castingPlayerId).withTriggeringSpell(castSpellEntry),
                        entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setTriggeringSpell(castSpellEntry);
                        }));
            }
        }

        // Face-up exiled cards' cast abilities.
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            if (exiledEntry.faceDown() || !castingPlayerId.equals(exiledEntry.ownerId())) {
                continue;
            }
            dispatchCardSpellAbilities(gameData, exiledEntry.card(), castingPlayerId, YOU_CAST, Zone.EXILE, event,
                    null);
        }

        // Eminence and the other command-zone cast abilities.
        for (Card card : new ArrayList<>(gameData.playerCommandZones.getOrDefault(castingPlayerId, List.of()))) {
            for (TriggeredAbility ability : triggerIndex.cardAbilities(gameData, card)) {
                SpellFilter filter = watchedSpellFilter(ability, YOU_CAST, Zone.COMMAND, false);
                if (filter == null) continue;
                requireNoOptions(ability, card);
                if (!predicateEvaluationService.matchesCardPredicate(spellCard, filter.spell(), null)) {
                    continue;
                }
                triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                        castingPlayerId, card.getName() + "'s ability",
                        ConditionContext.forCard(card, castingPlayerId), TriggerRecord.NONE));
            }
        }

        // The spell's own "when you cast this spell" abilities and the ones perpetually granted to it.
        StackEntry selfCastSpellEntry = castSpellEntry;
        ConditionContext selfCastContext = selfCastSpellEntry != null
                ? ConditionContext.forStackEntry(selfCastSpellEntry)
                : ConditionContext.forCard(spellCard, castingPlayerId);
        List<TriggeredAbility> ownCastAbilities = new ArrayList<>();
        for (TriggeredAbility ability : spellCard.getTriggeredAbilities()) {
            if (ability.hasWatchedTrigger(YOU_CAST_THIS, Zone.STACK)) {
                requireNoLimit(ability, spellCard);
                ownCastAbilities.add(ability);
            }
        }
        for (Ability granted : gameData.perpetualAbilities(spellCard.getId())) {
            if (granted instanceof TriggeredAbility triggered && triggered.hasTrigger(YOU_CAST_THIS, Zone.STACK)) {
                ownCastAbilities.add(triggered);
            }
        }
        for (TriggeredAbility ability : ownCastAbilities) {
            putSelfCastAbility(gameData, spellCard, castingPlayerId, castZone, selfCastSpellEntry, ability,
                    selfCastContext);
        }
    }

    /**
     * "Whenever [a player] plays a land" (Search the City, Juju Bubble, Dirtcowl Wurm) of the permanents, the face-up
     * planes and the abilities a resolution registered. A land an effect puts onto the battlefield isn't played.
     */
    private void landPlayed(GameData gameData, SpellEvent.LandPlayed event) {
        UUID playingPlayerId = event.playingPlayerId();
        Card landCard = event.landCard();
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(playingPlayerId)) {
                dispatchSpellWatch(gameData, perm, playerId, YOU_PLAY_LAND, event);
                if (!perm.getOriginalCard().getId().equals(landCard.getId())) {
                    dispatchSpellWatch(gameData, perm, playerId, YOU_PLAY_ANOTHER_LAND, event);
                }
            } else {
                dispatchSpellWatch(gameData, perm, playerId, OPPONENT_PLAYS_LAND, event);
            }
        });
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            Trigger watched = gameData.planechase.controllerId.equals(playingPlayerId)
                    ? YOU_PLAY_LAND : OPPONENT_PLAYS_LAND;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                dispatchCardSpellAbilities(gameData, object.getCard(), gameData.planechase.controllerId, watched,
                        Zone.BATTLEFIELD, event, object);
            }
        }
        collectTemporaryControllerLandPlayTriggers(gameData, event);
    }

    /**
     * The "whenever [a player] activates an ability" abilities an activation triggers, each whole (CR 603.3), once the
     * activated ability is on the stack and its costs are paid (CR 602.2b, 601.2i): a permanent that left the
     * battlefield to pay them has no ability left to trigger. Every activation ability of every permanent, and of every
     * card in a graveyard that has one there, is read the same way whatever its effects are: its event and player are
     * matched against the activation ({@link ActivationAbility#triggersOn}), its filters and payment and its event
     * condition are judged by {@link #activationTriggers}, and its record names the activation
     * ({@link #activationRecord}).
     */
    private void activated(GameData gameData, SpellEvent.Activated activation) {
        int lifePaid = activation.lifePaid();
        gameData.forEachPermanent((ownerId, perm) -> {
            for (ActivationAbility watching : activationAbilities(triggerIndex.permanentAbilities(gameData, perm),
                    Zone.BATTLEFIELD)) {
                if (!watching.triggersOn(activation, ownerId)) continue;
                ConditionContext context = ConditionContext.forPermanent(perm, ownerId)
                        .withTreasureManaSpentToActivate(activation.treasureManaSpent());
                if (!activationTriggers(gameData, perm, ownerId, watching, activation, lifePaid, context)) continue;
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                        watching.ability(), perm.getCard(), perm, ownerId, perm.getCard().getName() + "'s ability",
                        context, activationRecord(watching, activation, lifePaid)));
                if (triggered) {
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
            }
        });
        collectGraveyardActivationTriggers(gameData, activation, lifePaid);
    }


    /**
     * "Whenever you copy a spell" and "whenever an opponent copies a spell": a copy of an ability isn't a spell, so it
     * triggers neither.
     */
    private void copied(GameData gameData, SpellEvent.Copied event) {
        StackEntry copiedSpell = event.copy();
        if (copiedSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || copiedSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return;
        }
        UUID copyingPlayerId = event.copyingPlayerId();
        gameData.forEachPermanent((playerId, permanent) -> dispatchSpellWatch(gameData, permanent, playerId,
                playerId.equals(copyingPlayerId) ? YOU_COPY : OPPONENT_COPIES, event));
    }

    /**
     * Hands {@code ability}, a "when you cast this spell" ability of {@code spellCard} (its own, or one perpetually
     * granted to it), to the stack door: its event condition is judged here (CR 603.2), the door judges its intervening
     * "if" in {@code context}, the spell on the stack, puts it on the stack as one object (CR 603.3) and chooses its
     * targets as it goes there (CR 603.3d). The record names the spell ({@code spellEntry}, its X, kick and the zone it
     * was cast from) as its triggering spell, whatever its effects are; "copy it" reads it as the ability resolves, as
     * it last existed on the stack once it has left (CR 608.2h).
     */
    public void putSelfCastAbility(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                                    StackEntry spellEntry, TriggeredAbility ability, ConditionContext context) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return;
        }
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, spellCard, null,
                castingPlayerId, spellCard.getName() + "'s ability", context, entry -> {
                    entry.setSourceZone(castZone);
                    entry.setNonTargeting(true);
                    if (spellEntry != null) {
                        recordTriggeringSpell(gameData, entry, spellEntry);
                        entry.setKicked(spellEntry.wasKicked());
                    }
                }));
    }

    private void dispatchSuspendedExiledCardSpellCastTriggers(GameData gameData, UUID castingPlayerId,
                                                              SpellEvent.Cast context) {
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            UUID ownerId = exiledEntry.ownerId();
            if (ownerId.equals(castingPlayerId)) {
                continue;
            }
            if (gameData.exiledCardsWithNonSuspendTimeCounters.contains(exiledEntry.card().getId())) {
                continue;
            }
            Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledEntry.card().getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }

            Card card = exiledEntry.card();
            for (TriggeredAbility ability : card.getTriggeredAbilities()) {
                if (ability.hasWatchedTrigger(OPPONENT_CASTS, Zone.BATTLEFIELD)) {
                    requireNoLimit(ability, card);
                    collectSuspendedCastAbility(gameData, card, ownerId, context, ability);
                }
            }
        }
    }

    /**
     * Collects {@code ability}, a "whenever an opponent casts a spell" ability of the suspended card {@code card}, for
     * the cast {@code context}: its options are judged as it triggers, and its intervening "if" ("if this card is
     * suspended") stays around each effect and is checked again as it resolves (CR 603.4). The ability goes on the
     * stack as one object of its effects (CR 603.3), the spell's caster as "that player" (the entry's non-targeting
     * target, who decides Pardic Dragon's "that player may put a time counter on it") and the spell as its triggering
     * card.
     */
    private void collectSuspendedCastAbility(GameData gameData, Card card, UUID ownerId,
                                             SpellEvent.Cast context, TriggeredAbility ability) {
        ConditionContext conditionContext = ConditionContext.forCard(card, ownerId);
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
            return;
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card, null,
                ownerId, card.getName() + "'s ability", conditionContext, entry -> {
                    entry.setTargetId(context.castingPlayerId());
                    entry.setTriggeringCardId(context.spellCard().getId());
                    entry.setNonTargeting(true);
                }));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s ability triggers."));
        }
    }

    private boolean isNthMatchingSpell(GameData gameData, SpellFilter filter, UUID playerId) {
        long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, filter.spell(), null))
                .count();
        return matchingSpells == filter.nthThisTurn();
    }

    /**
     * The readers of a card that isn't a permanent (a spell's "when you cast this spell" and "when this spell is
     * countered" abilities, a suspended card's) judge no limit, which is kept on a permanent; a card with one there
     * fails loudly instead of triggering wrongly.
     */
    /**
     * Collects {@code emblem}'s cast abilities ("whenever you cast a creature or planeswalker spell, …", "whenever a
     * player casts a spell, …") for the spell {@code spellCast} names. Abilities of emblems function in the command
     * zone (CR 114.4), whatever zones their trigger conditions name. The emblem has no permanent, so the generic cast
     * collector judges each ability's spell filter, intervening "if" and target filter with the emblem as its source,
     * and puts it on the stack once: nothing makes an emblem's ability trigger additional times. An ability takes no
     * limit here.
     */
    private void collectEmblemCastAbilities(GameData gameData, Emblem emblem, SpellEvent.Cast spellCast) {
        for (TriggeredAbility ability : emblem.triggeredAbilities()) {
            for (TriggerCondition condition : ability.conditions()) {
                if (!(condition.trigger() instanceof SpellTrigger spell)
                        || spell.event() != SpellTrigger.SpellEvent.CAST
                        || !castByRelatedPlayer(spell.player(), emblem.controllerId(), spellCast.castingPlayerId())) {
                    continue;
                }
                requireNoLimit(ability, emblem.sourceCard());
                collectCastAbility(
                        new TriggerMatchContext(gameData, null, emblem.controllerId(), List.of(), emblem.sourceCard(),
                                (PlanarObject) null),
                        CastAbility.of(ability, spell.filter() == null ? SpellFilter.ANY : spell.filter()),
                        spellCast, false);
                break;
            }
        }
    }

    /** Whether {@code castingPlayerId} is the player {@code relation} names, relative to {@code controllerId}. */
    private static boolean castByRelatedPlayer(com.github.laxika.magicalvibes.model.trigger.PlayerRelation relation,
                                               UUID controllerId, UUID castingPlayerId) {
        return switch (relation) {
            case ANY -> true;
            case YOU -> controllerId.equals(castingPlayerId);
            case OPPONENT -> !controllerId.equals(castingPlayerId);
        };
    }

    /** As {@link #requireNoOptions}, for the graveyard and hand cast readers, which judge an intervening "if" (CR 603.4). */
    private static void requireOnlyInterveningIf(TriggeredAbility ability, Card card) {
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH
                || options.eventCondition() != null) {
            throw new IllegalStateException(card.getName() + "'s cast ability has options the graveyard reader "
                    + "doesn't judge: " + options);
        }
    }

    /** "Whenever a spell or ability you control counters a spell": the countering player's permanents. */
    private void countered(GameData gameData, SpellEvent.Countered event) {
        UUID counteringPlayerId = event.counteringPlayerId();
        if (counteringPlayerId == null) return;
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(counteringPlayerId)) {
                dispatchSpellWatch(gameData, perm, playerId, YOU_COUNTER, event);
            }
        });
    }

    /**
     * The "whenever a spell you control causes you to gain card advantage" abilities (Toofer, Keeper of the Full Grip)
     * of the permanents the spell's controller controls, once the spell finished resolving and gave that player card
     * advantage (the caller judges it). The abilities trigger on the spell's resolution, not its cast, so a spell its
     * controller didn't cast counts, and the source must be on the battlefield only as the spell finishes resolving
     * (CR 603.2).
     */
    private void cardAdvantage(GameData gameData, SpellEvent.CardAdvantage event) {
        UUID controllerId = event.spell().getControllerId();
        if (controllerId == null) return;
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(controllerId)) {
                dispatchSpellWatch(gameData, perm, playerId, CARD_ADVANTAGE, event);
            }
        });
    }

    /** "Whenever a spell you control is countered": the spell's controller's permanents. */
    private void spellCountered(GameData gameData, SpellEvent.SpellCountered event) {
        UUID spellControllerId = event.spellControllerId();
        if (spellControllerId == null) return;
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(spellControllerId)) {
                dispatchSpellWatch(gameData, perm, playerId, YOUR_SPELL_COUNTERED, event);
            }
        });
    }

    /** An instant or sorcery spell's own "when this spell is countered or doesn't resolve" abilities. */
    private void counteredOrFizzled(GameData gameData, SpellEvent.CounteredOrFizzled event) {
        StackEntry spellEntry = event.spell();
        if (spellEntry == null || spellEntry.getCard() == null || spellEntry.getControllerId() == null
                || (spellEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                && spellEntry.getEntryType() != StackEntryType.SORCERY_SPELL)) return;

        Card spellCard = spellEntry.getCard();
        for (TriggeredAbility ability : spellCard.getTriggeredAbilities()) {
            if (!ability.hasWatchedTrigger(THIS_COUNTERED_OR_FIZZLES, Zone.STACK)) {
                continue;
            }
            requireNoLimit(ability, spellCard);
            ConditionContext conditionContext = ConditionContext.forStackEntry(spellEntry);
            if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
                continue;
            }
            // The spell's own ability, controlled by the spell's controller, with the spell as it last existed on
            // the stack as its triggering spell ("copy it" copies that, CR 608.2h).
            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, spellCard, null,
                    spellEntry.getControllerId(), spellCard.getName() + "'s ability", conditionContext,
                    spellWatchRecord(event, null, null)));
        }
    }

    /**
     * The "whenever a mana ability of this creature resolves" abilities of the mana ability's source, its own and those
     * other permanents grant it (Tyvar the Bellicose), by {@link #collectUnfilteredAbility}: each grant is an instance
     * of its own with its own "triggers only once each turn" (CR 113.2c).
     */
    private void manaAbilityResolved(GameData gameData, SpellEvent.ManaAbilityResolved event) {
        Permanent sourcePermanent = event.source();
        UUID controllerId = event.activatingPlayerId();
        Card card = sourcePermanent.getCard();
        List<TriggeredAbility> abilities = new ArrayList<>(card.getTriggeredAbilities().stream()
                .filter(ability -> ability.hasWatchedTrigger(THIS_MANA_ABILITY_RESOLVES, Zone.BATTLEFIELD))
                .toList());
        abilities.addAll(grantedTriggeredAbilitySupport.grantedTriggeredAbilities(gameData, sourcePermanent, null,
                ability -> ability.hasWatchedTrigger(THIS_MANA_ABILITY_RESOLVES)));
        for (TriggeredAbility ability : abilities) {
            collectUnfilteredAbility(gameData, sourcePermanent, card, controllerId, event, ability,
                    UnaryOperator.identity(), null);
        }
    }

    /**
     * An activation ability ("whenever [a player] activates an ability") of a permanent or of a card in a graveyard,
     * with the trigger condition the activation collector reads it by. The collector judges its event and player
     * ({@link #triggersOn}), its filters ({@link AbilityTrigger#kind()}, {@link AbilityTrigger#source()},
     * {@link AbilityTrigger#stackEntry()}, {@link AbilityTrigger#payment()}) and its options itself.
     */
    private record ActivationAbility(TriggeredAbility ability, AbilityTrigger trigger) {

        /**
         * Whether {@code activation} is an event of this ability's trigger for a watcher {@code watcherControllerId}
         * controls: the activating player is the one the trigger names and the ability is of the event's kind (any
         * ability, one that isn't a mana ability, an exhaust ability, an embalm or eternalize ability).
         */
        boolean triggersOn(SpellEvent.Activated activation, UUID watcherControllerId) {
            boolean activatedByWatcherController = watcherControllerId.equals(activation.activatingPlayerId());
            boolean player = switch (trigger.player()) {
                case ANY -> true;
                case YOU -> activatedByWatcherController;
                case OPPONENT -> !activatedByWatcherController;
            };
            if (!player) {
                return false;
            }
            return switch (trigger.event()) {
                case ACTIVATED -> true;
                case NONMANA_ACTIVATED -> !activation.manaAbility();
                case EXHAUST_ACTIVATED -> activation.ability().isExhaustAbility();
                case ETERNALIZE_OR_EMBALM_ACTIVATED -> activation.ability().isEmbalmOrEternalize();
                case THIS_MANA_ABILITY_RESOLVES -> false;
            };
        }

    }

    /** The watches whose triggers the activation collector reads, in a zone each. */
    private static final Map<Zone, List<Trigger>> ACTIVATION_TRIGGERS = Map.of(
            Zone.BATTLEFIELD, List.of(Triggers.youActivateAbility(), Triggers.opponentActivatesAbility(),
                    Triggers.youActivateNonManaAbility(), Triggers.opponentActivatesNonManaAbility(),
                    Triggers.playerActivatesNonManaAbility(), Triggers.youActivateExhaustAbility(),
                    Triggers.youActivateEternalizeOrEmbalm()),
            Zone.GRAVEYARD, List.of(Triggers.youActivateExhaustAbility()));

    /**
     * The activation abilities among {@code abilities} that function in {@code zone}, in the order they were added:
     * those with a trigger condition that is an activation watch's trigger in that zone, or narrows it.
     */
    private static List<ActivationAbility> activationAbilities(List<TriggeredAbility> abilities, Zone zone) {
        List<ActivationAbility> watching = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            for (TriggerCondition condition : ability.conditions()) {
                if (condition.trigger() instanceof AbilityTrigger activation
                        && condition.zones().functionsIn(zone)
                        && ACTIVATION_TRIGGERS.getOrDefault(zone, List.of()).contains(activation.unfiltered())) {
                    watching.add(new ActivationAbility(ability, activation));
                    break;
                }
            }
        }
        return watching;
    }

    /**
     * The activation abilities that function in a graveyard (Afterburner Expert's "whenever you activate an exhaust
     * ability, return this card from your graveyard to the battlefield"), for the cards in every graveyard: their
     * owner is the "you" of the trigger. Such an ability goes to the stack door as written, with the same record as a
     * permanent's ({@link #activationRecord}); one with options, or with a filter judged from a permanent's point of
     * view, throws (no card has one).
     */
    private void collectGraveyardActivationTriggers(GameData gameData, SpellEvent.Activated activation, int lifePaid) {
        for (UUID ownerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                List<TriggeredAbility> written = new ArrayList<>(card.getTriggeredAbilities());
                for (Ability granted : gameData.perpetualAbilities(card.getId())) {
                    if (granted instanceof TriggeredAbility triggered) {
                        written.add(triggered);
                    }
                }
                if (activationAbilities(written, Zone.GRAVEYARD).isEmpty()) continue;
                for (ActivationAbility watching : activationAbilities(
                        gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card), Zone.GRAVEYARD)) {
                    if (!watching.triggersOn(activation, ownerId)) continue;
                    AbilityTrigger trigger = watching.trigger();
                    if (trigger.source() != null || trigger.stackEntry() != null) {
                        throw new IllegalStateException(card.getName() + "'s graveyard activation ability has a "
                                + "filter judged from a permanent's point of view: " + watching.ability());
                    }
                    requireNoOptions(watching.ability(), card);
                    if (trigger.kind() != null && !trigger.kind().matches(activation.ability())) continue;
                    if (!activationPaymentMatches(trigger, activation.abilityEntry(), lifePaid)) continue;
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            watching.ability(), card, null, ownerId, card.getName() + "'s ability",
                            ConditionContext.forCard(card, ownerId), activationRecord(watching, activation, lifePaid)));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    }
                }
            }
        }
    }

    /**
     * Whether {@code watching}, an ability of {@code watcher}, triggers on {@code activation}: the kind of ability ("a
     * loyalty ability", "without {T} in its activation cost"), the permanent whose ability it is ("an ability of an
     * artifact", judged as it last existed and from {@code watcher}'s point of view; an ability of a card that isn't a
     * permanent has none, CR 109.2), the activated ability on the stack with its targets ("that targets only Bill
     * Potts") and what was paid to activate it ("if life was paid to activate it", {@code lifePaid}) must match the
     * trigger's filters, and its event condition (CR 603.2) must be true in {@code context}. The stack door judges its
     * limit (CR 603.2h) and intervening "if" (CR 603.4); "if mana from a Treasure was spent to activate it" is a fact of
     * the activation, which {@code context} carries.
     */
    private boolean activationTriggers(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                       ActivationAbility watching, SpellEvent.Activated activation, int lifePaid,
                                       ConditionContext context) {
        AbilityTrigger trigger = watching.trigger();
        if (trigger.kind() != null && !trigger.kind().matches(activation.ability())) {
            return false;
        }
        if (trigger.source() != null) {
            if (activation.activatedPermanent() == null) return false;
            if (!predicateEvaluationService.matchesPermanentPredicate(activation.activatedPermanent(),
                    trigger.source(), activatedPermanentFilterContext(gameData, watcher, watcherControllerId,
                            activation.activatedPermanent()))) {
                return false;
            }
        }
        if (trigger.stackEntry() != null && !targetLegalityService.matchesStackEntryPredicate(
                gameData, activation.abilityEntry(), trigger.stackEntry(), watcherControllerId, watcher)) {
            return false;
        }
        if (!activationPaymentMatches(trigger, activation.abilityEntry(), lifePaid)) {
            return false;
        }
        TriggerOptions options = watching.ability().options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException(watcher.getCard().getName() + "'s activation ability has a multiplicity "
                    + "the activation collector doesn't judge: " + options);
        }
        return conditionEvaluationService.eventConditionMet(gameData, options, context);
    }

    /**
     * The filter context {@code watcher} judges {@code activatedPermanent}, the permanent whose ability was activated,
     * with. An Aura or Equipment that was attached to that permanent when paying the costs took it off the battlefield
     * is attached to nothing as the ability becomes activated (it stays on the battlefield until state-based actions
     * are checked, CR 704.5m and CR 704.5n), so its "an ability of enchanted artifact" or "of equipped creature"
     * names no permanent: it is judged as it is now, unattached, not by the permanent it was last attached to.
     */
    private FilterContext activatedPermanentFilterContext(GameData gameData, Permanent watcher,
                                                          UUID watcherControllerId, Permanent activatedPermanent) {
        if (!activatedPermanent.getId().equals(watcher.getAttachedTo())
                || gameQueryService.findPermanentById(gameData, activatedPermanent.getId()) != null) {
            return watcherFilterContext(gameData, watcher, watcherControllerId);
        }
        Permanent unattached = new Permanent(watcher);
        unattached.setAttachedTo(null);
        return FilterContext.of(gameData)
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentSnapshot(unattached);
    }

    /**
     * Whether the activation on the stack as {@code abilityEntry} paid what the payment filter of {@code trigger}
     * requires: life ({@code lifePaid}, Verrak, Warped Sengir), or one or more permanents sacrificed for its costs
     * (Ashnod the Uncaring): the entry records every one, the source, the chosen ones and the granting Equipment
     * alike; true without one.
     */
    private static boolean activationPaymentMatches(AbilityTrigger trigger, StackEntry abilityEntry, int lifePaid) {
        AbilityTrigger.ActivationPayment payment = trigger.payment();
        if (payment == null) {
            return true;
        }
        return switch (payment) {
            case LIFE -> lifePaid > 0;
            case SACRIFICE -> !abilityEntry.getSacrificedCardIds().isEmpty();
        };
    }

    /**
     * The record of {@code watching}, an activation ability, for {@code activation}, stamped on its entry whatever its
     * effects are: the activated ability on the stack as its triggering spell and the {@link ActivatedAbility} ("copy
     * that ability" copies it with its targets and X for this ability's controller, CR 707.10), the permanent whose
     * ability it is with its controller ("that artifact's controller", read as the ability resolves or as the
     * permanent last existed, CR 608.2h), the life paid to activate it as its event value ("pay that much life") and
     * the activating player as the non-targeting "that player" of an "an opponent activates" trigger. An effect that acts
     * on "that ability" is aimed at it by the stack door.
     */
    private static TriggerRecord activationRecord(ActivationAbility watching, SpellEvent.Activated activation, int lifePaid) {
        return entry -> {
            entry.setNonTargeting(true);
            if (watching.trigger().player() == com.github.laxika.magicalvibes.model.trigger.PlayerRelation.OPPONENT) {
                entry.setTargetId(activation.activatingPlayerId());
            }
            entry.setTriggeringSpell(activation.abilityEntry());
            entry.setTriggeringActivatedAbility(activation.ability());
            if (lifePaid > 0) {
                entry.setEventValue(lifePaid);
            }
            if (activation.activatedPermanent() != null) {
                entry.setTriggeringPermanentId(activation.activatedPermanent().getId());
            }
            entry.setTriggeringPermanentControllerId(activation.activatedPermanentControllerId());
        };
    }

    private void collectTemporaryControllerSpellCastTriggers(GameData gameData, SpellEvent.Cast cast) {
        Card spellCard = cast.spellCard();
        UUID castingPlayerId = cast.castingPlayerId();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.controllerId().equals(castingPlayerId)) {
                continue;
            }
            // A cast trigger with a spell filter ("until end of turn, whenever you cast an instant or sorcery spell")
            // is judged and put on the stack by the generic cast collector, which gives it the spell as "that spell";
            // its "a card exiled with this" (Apple of Eden, Isu Relic) names the permanent that registered it.
            SpellFilter filter = watchedSpellFilter(watcher.ability(), YOU_CAST, Zone.BATTLEFIELD, true);
            if (filter != null) {
                requireNoLimit(watcher.ability(), watcher.sourceCard());
                collectCastAbility(registeredMatch(gameData, watcher), CastAbility.of(watcher.ability(), filter), cast,
                        false);
                continue;
            }
            if (!watcher.ability().hasTrigger(YOU_CAST)) {
                continue;
            }

            StackEntry triggerEntry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            triggerEntry.setNonTargeting(true);
            triggerEntry.setTriggeringCardId(spellCard.getId());
            if (watcher.targetFilter() != null) {
                // Its target, restricted by the registration's filter, is chosen as it goes on the stack (CR 603.3d).
                triggerEntry.setTargetFilter(watcher.targetFilter());
                triggeredAbilityStackService.putMade(gameData, triggerEntry);
            } else {
                triggeredAbilityStackService.putDelayed(gameData, triggerEntry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * The abilities a resolving ability registered that trigger whenever a player casts a spell: each goes on the stack
     * as written, recording the spell as its triggering spell and the caster as its triggering player and non-targeting
     * "that player", whatever the effects are. "Whenever they cast a spell" (Nuka-Nuke Launcher) is the filter
     * {@link StackEntryControlledByWatchedPlayerPredicate}, judged against the player the registration recorded; this
     * reader judges no other spell filter.
     */
    private void collectTemporaryGlobalSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        for (TemporaryGlobalTriggeredAbility watcher
                : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            SpellFilter filter = watchedSpellFilter(watcher.ability(), PLAYER_CASTS, Zone.BATTLEFIELD, true);
            if (filter == null && !watcher.ability().hasTrigger(PLAYER_CASTS)) {
                continue;
            }
            if (filter != null) {
                if (!filter.equals(SpellFilter.ANY.withStackEntry(new StackEntryControlledByWatchedPlayerPredicate()))) {
                    throw new IllegalStateException("The registered any-player cast reader judges only the watched "
                            + "player: " + watcher.ability());
                }
                if (!castingPlayerId.equals(watcher.watchedPlayerId())) {
                    continue;
                }
            }
            requireNoLimit(watcher.ability(), watcher.sourceCard());

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setTargetId(castingPlayerId);
            entry.setTriggeringPlayerId(castingPlayerId);
            entry.setTriggeringCardId(spellCard.getId());
            StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
            if (spellEntry != null) {
                entry.setTriggeringSpell(spellEntry);
            }
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global spell-cast trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * The abilities a resolving ability registered that trigger whenever their controller plays a land, read as the
     * land-play reader reads a printed one:
     * "a land exiled with this" (Apple of Eden, Isu Relic) names the permanent that registered the ability.
     */
    private void collectTemporaryControllerLandPlayTriggers(GameData gameData, SpellEvent.LandPlayed landPlayed) {
        UUID playingPlayerId = landPlayed.playingPlayerId();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.controllerId().equals(playingPlayerId)) {
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(watcher.ability(),
                    YOU_PLAY_LAND, Zone.BATTLEFIELD, true);
            if (landPlay == null) {
                if (!watcher.ability().hasTrigger(YOU_PLAY_LAND)) {
                    continue;
                }
                landPlay = (LandPlayTrigger) YOU_PLAY_LAND;
            }
            requireNoLimit(watcher.ability(), watcher.sourceCard());
            collectLandPlayAbility(registeredMatch(gameData, watcher), watcher.ability(), landPlay, landPlayed);
        }
    }

    /**
     * "When you spend this mana to cast ..." riders on a mana ability are delayed triggers created when the
     * mana is produced (CR 106.6), so they still fire if the producing permanent has left the battlefield
     * by the time the mana is spent. Only those riders are collected from the departed source's
     * last-known information.
     */
    private void dispatchDepartedManaSourceSpellCastTriggers(GameData gameData, Card spellCard,
                                                              UUID castingPlayerId, SpellEvent ctx) {
        for (UUID sourceId : List.copyOf(gameData.spellCastManaSourceIds.getOrDefault(spellCard.getId(), Set.of()))) {
            if (gameQueryService.findPermanentById(gameData, sourceId) != null) continue;
            Permanent departed = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(sourceId)).findFirst().orElse(null);
            if (departed == null) continue;
            for (TriggeredAbility ability : departed.getCard().getTriggeredAbilities()) {
                SpellFilter filter = watchedSpellFilter(ability, YOU_CAST, Zone.BATTLEFIELD, false);
                if (filter != null && filter.paidWithManaFromThis()) {
                    collectSpellAbility(gameData, departed, departed.getCard(), castingPlayerId,
                            YOU_CAST, ctx, ability, filter, null);
                }
            }
        }
    }

    /**
     * Collects a "whenever [a player] casts a [quality] spell" ability: judges its spell filter and event condition and
     * hands it to the stack door, which judges its intervening "if" and limit, puts it on the stack and chooses its mode
     * and targets as it does (CR 603.3c, 603.3d). {@code perManaFromSource} says whether a "when you spend this mana to
     * cast …" rider is collected once for each mana its source produced that was spent on the spell: such a rider on a
     * mana ability is a separate delayed trigger for each of them (CR 106.6), which the "whenever you cast" collector
     * reads.
     *
     * @return whether the ability triggered
     */
    boolean collectCastAbility(TriggerMatchContext match, CastAbility ability, SpellEvent.Cast sc,
                                      boolean perManaFromSource) {
        int copies = perManaFromSource && ability.filter().paidWithManaFromThis() && match.permanent() != null
                ? match.gameData().getSpellCastManaSourceAmount(sc.spellCard().getId(), match.permanent().getId())
                : 1;
        boolean collected = false;
        for (int i = 0; i < Math.max(1, copies); i++) {
            collected |= handleGenericSpellCastTrigger(match, ability, sc);
        }
        return collected;
    }

    /**
     * Collects a "whenever a spell you control causes you to gain card advantage" ability (Toofer, Keeper of the Full
     * Grip) for the spell {@code event} names, which just finished resolving and gave the ability's controller card
     * advantage: its event condition is judged here and the stack door puts it on the stack as written (CR 603.3), the
     * spell recorded as the entry's triggering spell.
     *
     * @return whether the ability triggered
     */
    boolean collectCardAdvantageAbility(TriggerMatchContext match, TriggeredAbility ability,
                                                SpellEvent.CardAdvantage event) {
        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : match.permanent().getCard();
        ConditionContext context = match.permanent() == null
                ? ConditionContext.forCard(sourceCard, match.controllerId())
                : ConditionContext.forPermanent(match.permanent(), match.controllerId());
        if (!conditionEvaluationService.eventConditionMet(match.gameData(), ability.options(), context)) {
            return false;
        }
        StackEntry spell = event.spell();
        return triggeredAbilityStackService.put(match.gameData(), new TriggeredAbilityRequest(ability, sourceCard,
                match.permanent(), match.controllerId(), abilityDescription(sourceCard), context, entry -> {
                    entry.setNonTargeting(true);
                    entry.setTriggeringCardId(spell.getTargetableId());
                    entry.setTriggeringSpell(spell);
                }));
    }

    private int kickedCount(StackEntry spellEntry) {
        boolean nonKickerAdditionalCost = spellEntry.getCard().getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect.KickerEffect kicker
                        && !kicker.countsAsKicker());
        int count = spellEntry.isKicked() && !nonKickerAdditionalCost
                && !spellEntry.getCard().getKeywords().contains(Keyword.BARGAIN) ? 1 : 0;
        int repeatedMultikickerCount = spellEntry.getCard().getCastingCosts().stream()
                .filter(RepeatableAdditionalManaCost.class::isInstance)
                .map(RepeatableAdditionalManaCost.class::cast)
                .mapToInt(cost -> cost.multikickerPaymentCount(spellEntry.getRepeatedAdditionalCosts()))
                .sum();
        return count + repeatedMultikickerCount;
    }

    /**
     * Collects a "whenever [a player] copies a [quality] spell" ability: judges the copied spell and its stack object
     * against the copy trigger's {@code filter} (a copy isn't cast, so that is all the filter says) and hands the
     * ability to the stack door, the copy recorded as its triggering spell, the copying player as "that player" and the
     * copy's mana value as the event's value, whatever its effects are; its targets are chosen as it goes on the stack
     * (CR 603.3d).
     *
     * @return whether the ability triggered
     */
    boolean collectCopyAbility(TriggerMatchContext match, TriggeredAbility ability, SpellFilter filter,
                                       SpellEvent.Copied copied) {
        TriggerContext.SpellCopy spellCopy = new TriggerContext.SpellCopy(copied.copy(), copied.copyingPlayerId());
        boolean isSpell = switch (spellCopy.copiedSpell().getEntryType()) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                 ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            default -> false;
        };
        if (!isSpell) {
            return false;
        }
        if (filter.stackEntry() != null
                && !predicateEvaluationService.matchesStackEntryPredicate(
                spellCopy.copiedSpell(), filter.stackEntry(), match.permanent().getAttachedTo())) {
            return false;
        }
        if (filter.spell() != null
                && !predicateEvaluationService.matchesCardPredicate(
                spellCopy.copiedSpell().getCard(), filter.spell(),
                match.permanent().getOriginalCard().getId(), match.gameData(),
                spellCopy.copyingPlayerId())) {
            return false;
        }
        ConditionContext context = ConditionContext.forPermanent(match.permanent(), match.controllerId())
                .withTriggeringSpell(spellCopy.copiedSpell());
        if (!conditionEvaluationService.eventConditionMet(match.gameData(), ability.options(), context)) {
            return false;
        }
        Card copiedCard = spellCopy.copiedSpell().getCard();
        int copiedSpellManaValue = copiedCard.getManaValue()
                + (copiedCard.getParsedManaCost() == null ? 0
                : spellCopy.copiedSpell().getXValue() * copiedCard.getParsedManaCost().getXSymbolCount());
        Card sourceCard = match.permanent().getCard();
        return triggeredAbilityStackService.put(match.gameData(), new TriggeredAbilityRequest(ability, sourceCard,
                match.permanent(), match.controllerId(), sourceCard.getName() + "'s ability", context, entry -> {
                    entry.setTargetId(spellCopy.copyingPlayerId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringCardId(copiedCard.getId());
                    entry.setTriggeringSpell(spellCopy.copiedSpell());
                    entry.setEventValue(copiedSpellManaValue);
                }).withEvent(spellCopy));
    }

    private static boolean mentionsXInManaCost(CardPredicate predicate) {
        return switch (predicate) {
            case null -> false;
            case CardHasXInManaCostPredicate ignored -> true;
            case CardAllOfPredicate allOf -> allOf.predicates().stream()
                    .anyMatch(SpellTriggers::mentionsXInManaCost);
            case CardAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SpellTriggers::mentionsXInManaCost);
            case CardNotPredicate not -> mentionsXInManaCost(not.predicate());
            default -> false;
        };
    }

    private boolean handleGenericSpellCastTrigger(TriggerMatchContext match, CastAbility trigger,
                                                    SpellEvent.Cast spellCast) {
        return handleGenericSpellCastTrigger(match, trigger, spellCast.spellCard(), spellCast.castingPlayerId(),
                spellCast.exiledSourcePermanentId(), spellCast.castZone());
    }

    /**
     * The spell as it exists on the stack: an {@code {X}} in its mana cost is the chosen X value (CR 202.3e),
     * so a mana value filter sees the real total. Returns the card itself when there is no chosen X.
     */
    private static Card adventureHalfOnStack(StackEntry spellEntry, Card spellCard) {
        if (spellEntry == null || !(spellEntry.isCastWithAdventure() || spellEntry.isCastWithOmen())) {
            return spellCard;
        }
        Card physical = spellEntry.getPhysicalCard() != null ? spellEntry.getPhysicalCard() : spellCard;
        return physical.getBackFaceCard() != null ? physical.getBackFaceCard() : spellCard;
    }

    public static Card spellOnStackWithChosenX(Card spellCard, Integer chosenX) {
        String manaCost = spellCard.getManaCost();
        if (chosenX == null || manaCost == null || !manaCost.contains("{X}")) {
            return spellCard;
        }
        Card onStack = spellCard.createRuntimeCopy();
        onStack.setManaCost(manaCost.replace("{X}", "{" + chosenX + "}"));
        return onStack;
    }

    private boolean handleGenericSpellCastTrigger(TriggerMatchContext match, CastAbility trigger,
                                                    Card spellCard, UUID castingPlayerId,
                                                    UUID exiledSourcePermanentId, Zone castZone) {
        // An ability the source gives the spell ("spells you cast … have cascade, cascade") is the spell's: the spell is
        // its source (CR 113.7) and each instance triggers separately (CR 702.85c).
        if (trigger.filter().grantedToTheSpell() && match.sourceCard() != spellCard) {
            boolean queued = false;
            TriggerMatchContext spellSourced = new TriggerMatchContext(match.gameData(), null,
                    match.controllerId(), List.of(), spellCard, match.sourcePlanarObject(),
                    match.onceEachTurnActionKey(), match.triggersOnlyOnceEachTurn());
            for (EffectRegistration cascade : trigger.ability().effects()) {
                queued |= handleGenericSpellCastTrigger(spellSourced,
                        CastAbility.of(trigger.ability().withEffects(List.of(cascade)), trigger.filter()),
                        spellCard, castingPlayerId, exiledSourcePermanentId, castZone);
            }
            return queued;
        }
        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : match.permanent().getCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        UUID sourceOriginalCardId = match.permanent() == null
                ? sourceCard.getId() : match.permanent().getOriginalCard().getId();

        if (trigger.filter().expend() > 0) {
            int totalManaSpent = match.gameData().getManaSpentToCastSpellsThisTurn(castingPlayerId);
            int manaSpentOnThisSpell = match.gameData().getSpellCastManaSpent(spellCard.getId());
            if (totalManaSpent < trigger.filter().expend()
                    || totalManaSpent - manaSpentOnThisSpell >= trigger.filter().expend()) {
                return false;
            }
        }
        if (trigger.filter().paidWithManaFromThis()
                && (sourcePermanentId == null
                || !match.gameData().spellCastUsedManaFromSource(spellCard.getId(), sourcePermanentId))) {
            return false;
        }
        if (trigger.filter().paidWithTreasureMana()
                && !match.gameData().spellCastUsedTreasureMana(spellCard.getId())) {
            return false;
        }

        // "Whenever you cast a spell during an opponent's turn" — the source's controller must not be
        // the active player when the spell is cast (Glen Elendra Pranksters).
        if ((trigger.filter().turn() == SpellFilter.Turn.OPPONENTS)
                && match.controllerId().equals(match.gameData().activePlayerId)) return false;

        // "Whenever an opponent casts a spell during your turn" — the source's controller must be
        // the active player when the spell is cast (Eyes of the Wisent).
        if ((trigger.filter().turn() == SpellFilter.Turn.YOURS)
                && !match.controllerId().equals(match.gameData().activePlayerId)) return false;

        // "Whenever a player casts a spell, if it isn't that player's turn" — compare the
        // caster with the active player, rather than the trigger source's controller.
        if ((trigger.filter().turn() == SpellFilter.Turn.NOT_CASTERS)
                && match.gameData().activePlayerId != null
                && match.gameData().activePlayerId.equals(castingPlayerId)) return false;
        // Check the timing of the cast event itself, rather than the timing at which this
        // triggered ability resolves.
        if (trigger.filter().duringCombat()
                && (match.gameData().currentStep == null || !match.gameData().currentStep.isCombatPhase())) {
            return false;
        }
        if (trigger.filter().castFrom() != null && castZone != trigger.filter().castFrom()
                || trigger.filter().notCastFrom() != null && castZone == trigger.filter().notCastFrom()) {
            return false;
        }

        StackEntry triggeringSpell = findStackEntryForCard(match.gameData(), spellCard.getId());
        // "Whenever you cast a kicked spell" (CR 702.33d): the number of times it was kicked is the event value.
        int kickCount = (trigger.filter().kicker() != SpellFilter.Kicker.ANY) && triggeringSpell != null ? kickedCount(triggeringSpell) : 0;
        if ((trigger.filter().kicker() != SpellFilter.Kicker.ANY) && kickCount == 0) {
            return false;
        }
        Integer spellXValue = triggeringSpell == null ? null : triggeringSpell.getXValue();
        // A spell cast as an Adventure has only the adventure half's characteristics while on the stack
        // (CR 716.3b), so its colors, types and mana cost are read from that half.
        Card spellFace = adventureHalfOnStack(triggeringSpell, spellCard);
        // A spell keeps the {X} in its mana cost on the stack (CR 107.3), so a filter asking for it
        // reads the printed cost rather than the cost with the chosen X substituted.
        Card evaluatedSpell = mentionsXInManaCost(trigger.filter().spell())
                ? spellFace : spellOnStackWithChosenX(spellFace, spellXValue);
        if (!predicateEvaluationService.matchesCardPredicate(evaluatedSpell,
                trigger.filter().spell(), sourceOriginalCardId,
                match.gameData(), castingPlayerId, null, null, spellXValue)) return false;

        if (trigger.filter().twoOrMoreCardTypes()
                && !hasAtLeastTwoCardTypes(match.gameData(), spellCard, castingPlayerId)) {
            return false;
        }

        if (trigger.filter().nthThisTurn() > 0 && !isNthMatchingSpell(match.gameData(), trigger, castingPlayerId)) {
            return false;
        }

        if (trigger.filter().nthOrLaterThisTurn() > 0
                && !hasAtLeastMatchingSpell(match.gameData(), trigger, castingPlayerId)) {
            return false;
        }

        boolean interveningSpellManaValueCondition =
                trigger.interveningIf() instanceof SpellManaValueEqualsSourceCounters;
        int interveningSpellManaValue = interveningSpellManaValueCondition
                ? spellManaValue(match.gameData(), spellCard) : 0;
        // The conditions on the spell ("if at least four mana was spent to cast it") read it as it is on the stack, its
        // mana spent recorded as it became cast; the entry keeps it as its triggering spell for the check as it resolves.
        ConditionContext interveningContext = (match.permanent() == null
                ? ConditionContext.forCard(sourceCard, match.controllerId())
                : ConditionContext.forPermanent(match.permanent(), match.controllerId()))
                .withXValue(spellXValue == null ? 0 : spellXValue)
                .withTriggeringSpell(triggeringSpell);
        if (interveningSpellManaValueCondition) {
            interveningContext = interveningContext.withEventValue(interveningSpellManaValue);
        }
        // "Whenever you cast a spell while …" (CR 603.2): the cast event itself requires the condition, so it is
        // checked only now, never again as the ability resolves.
        if (trigger.eventCondition() != null
                && !conditionEvaluationService.isMet(match.gameData(), trigger.eventCondition(),
                interveningContext)) {
            return false;
        }

        // Repartee-style condition on the cast spell's chosen targets (e.g. "targets a creature"),
        // or source-relative mana-value gates (e.g. Imminent Doom — MV equals counters on source).
        if (trigger.filter().stackEntry() != null) {
            StackEntry spellEntry = findStackEntryForCard(match.gameData(), spellCard.getId());
            if (spellEntry == null) return false;
            // Evaluated from the trigger source's controller, so "you"/"you control" in the predicate
            // means the ability's controller — not the caster (Reparations, an opponent-cast trigger).
            if (!matchesCastSpellPredicate(match, spellEntry, trigger.filter().stackEntry(),
                    exiledSourcePermanentId)) return false;
        }

        // A permanent target restricted by "that spell's mana value" (Skyfire Kirin) is chosen as the ability goes on
        // the stack (CR 603.3d), against the spell's mana value as the ability's X. Otherwise the ability's X is the
        // spell's: an effect's own filter "with that spell's mana value" (Celestial Kirin's destroy, Infernal Kirin's
        // discard), "the amount of mana spent to cast that spell" and "for each color of mana spent to cast that spell"
        // read the triggering spell as the ability resolves.
        TargetFilter abilityTargetFilter = trigger.targetFilter() != null ? trigger.targetFilter()
                : boundTargetFilter(sourceCard, trigger.effects());
        int abilityX = readsManaValueX(abilityTargetFilter)
                ? spellManaValue(match.gameData(), spellCard)
                : spellXValue != null ? spellXValue : 0;
        // The cast event's value, recorded on every entry and choice whatever the effects are: the number of kicks for a
        // kicked-spell ability, else the spell's mana value as the ability triggers (with the chosen X, CR 202.3e). An
        // effect reads "that spell's mana value" as it resolves (TargetSpellManaValue: the live spell, else the entry's
        // snapshot of it, CR 608.2h), so this value is only the fallback where neither is kept.
        int eventValue = interveningSpellManaValueCondition ? interveningSpellManaValue
                : (trigger.filter().kicker() != SpellFilter.Kicker.ANY) ? kickCount
                : spellManaValue(match.gameData(), spellCard);
        List<UUID> spellTargets = new ArrayList<>();
        if (triggeringSpell != null) {
            if (triggeringSpell.getTargetId() != null) {
                spellTargets.add(triggeringSpell.getTargetId());
            }
            spellTargets.addAll(triggeringSpell.getDeclaredTargetIds());
        }
        int castIndex = match.gameData().getSpellCastIndexThisTurn(castingPlayerId, spellCard.getId());
        int turnCastIndex = match.gameData().getSpellCastOrdinalThisTurn(spellCard.getId()) - 1;
        Permanent source = match.permanent();
        com.github.laxika.magicalvibes.model.planar.PlanarObject planarObject = match.sourcePlanarObject();
        // What the cast event was, whatever the effects are: "that player" is the caster, the entry's non-targeting
        // player; "that spell" is its triggering card and spell ("counter that spell", "copy it", Bloodlord of
        // Vaasgoth's "it" and "its targets" as the spell was cast), as it last existed once it has left (CR 608.2h);
        // the source as the ability triggered, its last known information once it has left the battlefield and what a
        // transform instruction compares (CR 701.27f).
        TriggerRecord record = entry -> {
            entry.setTargetId(castingPlayerId);
            entry.setNonTargeting(true);
            entry.setTriggeringPlayerId(castingPlayerId);
            entry.setCounteredSpellControllerId(castingPlayerId);
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(triggeringSpell);
            entry.setTriggeringSpellCastIndex(castIndex);
            entry.setTriggeringSpellTurnCastIndex(turnCastIndex);
            if (triggeringSpell != null) {
                entry.setTriggeringSpellTargetIds(List.copyOf(spellTargets));
            }
            entry.setTriggeringCardSnapshot(spellCard);
            entry.setEventValue(eventValue);
            entry.setXValue(abilityX);
            if (source != null) {
                entry.setSourcePermanentSnapshot(new Permanent(source));
            }
            if (planarObject != null) {
                entry.setSourcePlanarObject(planarObject.copy());
            }
        };
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(trigger.ability(), sourceCard, source,
                match.controllerId(), abilityDescription(sourceCard), interveningContext, record)
                .withEvent(new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone,
                        exiledSourcePermanentId));
        boolean triggered = triggeredAbilityStackService.put(match.gameData(), request);
        // "Whenever a player kicks a spell": once for each time it was kicked.
        for (int kick = 1; triggered && trigger.filter().kicker() == SpellFilter.Kicker.EACH_KICK && kick < kickCount;
             kick++) {
            triggeredAbilityStackService.put(match.gameData(), request);
        }
        return triggered;
    }

    /**
     * The stack description of an ability of {@code sourceCard}: an emblem's ability (its source is the emblem's card,
     * {@link CardType#EMBLEM}) is the emblem's, any other the card's.
     */
    private static String abilityDescription(Card sourceCard) {
        return sourceCard.getName() + (sourceCard.hasType(CardType.EMBLEM) ? "'s emblem" : "'s ability");
    }

    /** Whether {@code filter} restricts a permanent target by the X the ability goes on the stack with. */
    private static boolean readsManaValueX(TargetFilter filter) {
        return filter instanceof PermanentPredicateTargetFilter permanentFilter
                && readsManaValueX(permanentFilter.predicate());
    }

    private static boolean readsManaValueX(PermanentPredicate predicate) {
        return switch (predicate) {
            case PermanentManaValueEqualsXPredicate ignored -> true;
            case PermanentMaxManaValueXPredicate ignored -> true;
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .anyMatch(SpellTriggers::readsManaValueX);
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SpellTriggers::readsManaValueX);
            case PermanentNotPredicate not -> readsManaValueX(not.predicate());
            case null, default -> false;
        };
    }


    /**
     * The filter of the target group the ability's effects are bound to, or {@code null} for none: a "whenever you
     * cast a spell" ability declares its target as a target group of the card ({@code target(filter).addTrigger(…)}),
     * bound to its effect or to the "you may" / "you may pay" effect around it.
     */
    private static TargetFilter boundTargetFilter(Card sourceCard, List<CardEffect> effects) {
        List<Integer> groups = boundTargetGroups(sourceCard, effects);
        return groups.isEmpty() ? null : sourceCard.getTargetGroups().get(groups.getFirst()).getFilter();
    }

    /**
     * The indexes of the target groups the ability's effects are bound to (directly, or through the "you may" / "you
     * may pay" effect around them), in their effects' order, each once; empty for none.
     */
    private static List<Integer> boundTargetGroups(Card sourceCard, List<CardEffect> effects) {
        List<Integer> indexes = new ArrayList<>();
        List<com.github.laxika.magicalvibes.model.TargetGroup> groups = sourceCard.getTargetGroups();
        for (CardEffect effect : effects) {
            int index = sourceCard.getEffectTargetIndex(effect);
            for (int group = 0; index < 0 && group < groups.size(); group++) {
                for (CardEffect candidate : sourceCard.getEffectsBoundToTargetGroup(group)) {
                    CardEffect wrapped = candidate instanceof MayEffect may ? may.wrapped()
                            : candidate instanceof MayPayManaEffect pay ? pay.wrapped() : null;
                    if (wrapped == effect || wrapped instanceof SequenceEffect sequence
                            && sequence.steps().stream().anyMatch(step -> step == effect)) {
                        index = group;
                        break;
                    }
                }
            }
            if (index >= 0 && index < groups.size() && !indexes.contains(index)) {
                indexes.add(index);
            }
        }
        return indexes;
    }

    private boolean matchesCastSpellPredicate(TriggerMatchContext match, StackEntry spellEntry,
                                               StackEntryPredicate predicate, UUID exiledSourcePermanentId) {
        // "A card exiled with this": the card was cast from exile while exiled with the ability's source, the
        // permanent, the face-up plane (The Matrix of Time) or the permanent that registered the ability (Apple of
        // Eden, Isu Relic), which the cast event names since the card has left exile.
        if (predicate instanceof StackEntryIsCardExiledWithSourcePredicate) {
            UUID sourceId = match.exiledWithSourceId();
            return sourceId != null && sourceId.equals(exiledSourcePermanentId);
        }
        if (predicate instanceof StackEntryCouldTargetAnotherPermanentPredicate couldTarget) {
            return couldTargetAnotherPermanent(match, spellEntry, couldTarget);
        }
        if (predicate instanceof StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().allMatch(nested ->
                    matchesCastSpellPredicate(match, spellEntry, nested, exiledSourcePermanentId));
        }
        return targetLegalityService.matchesStackEntryPredicate(match.gameData(), spellEntry,
                predicate, match.controllerId(), match.permanent());
    }

    /**
     * Whether {@code spellEntry} could target a permanent matching the predicate's filter, judged from the ability's
     * controller, other than the permanents it targets (and, for "another opponent", controlled by a player other than
     * its target's controller): an intervening "if" (CR 603.4) the copy effect checks again as it resolves.
     */
    private boolean couldTargetAnotherPermanent(TriggerMatchContext match, StackEntry spellEntry,
                                                StackEntryCouldTargetAnotherPermanentPredicate predicate) {
        GameData gameData = match.gameData();
        Set<UUID> spellTargets = new java.util.HashSet<>(spellEntry.getDeclaredTargetIds());
        if (spellEntry.getTargetId() != null) {
            spellTargets.add(spellEntry.getTargetId());
        }
        Set<UUID> targetControllers = new java.util.HashSet<>();
        for (UUID targetId : spellTargets) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                targetControllers.add(controllerId);
            }
        }
        Set<UUID> spellTargetableIds = Set.copyOf(validTargetService.computeValidTargetsForSpell(gameData,
                spellEntry.getCard(), spellEntry.getControllerId(), List.of(), spellEntry.getXValue(), null)
                .validPermanentIds());
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(match.controllerId());
        for (UUID permanentId : spellTargetableIds) {
            if (spellTargets.contains(permanentId)) {
                continue;
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
            if (permanent == null || controllerId == null
                    || predicate.otherThanTargetController() && targetControllers.contains(controllerId)) {
                continue;
            }
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, predicate.filter(), filterContext)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAtLeastTwoCardTypes(GameData gameData, Card card, UUID playerId) {
        int cardTypeCount = 0;
        for (CardType cardType : CardType.values()) {
            if (gameQueryService.cardHasType(card, cardType, gameData, playerId)
                    && ++cardTypeCount >= 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isNthMatchingSpell(GameData gameData, CastAbility trigger, UUID playerId) {
        return matchingSpellCount(gameData, trigger, playerId) == trigger.filter().nthThisTurn();
    }

    private boolean hasAtLeastMatchingSpell(GameData gameData, CastAbility trigger, UUID playerId) {
        return matchingSpellCount(gameData, trigger, playerId) >= trigger.filter().nthOrLaterThisTurn();
    }

    /**
     * The spells this turn that match the trigger's spell filter: the casting player's, or every player's for "the
     * first noncreature spell of a turn", only those cast from the trigger's cast zone when it names one.
     */
    private long matchingSpellCount(GameData gameData, CastAbility trigger, UUID playerId) {
        List<UUID> players = trigger.filter().everyPlayersSpells() ? List.copyOf(gameData.playerIds) : List.of(playerId);
        long matchingSpells = 0;
        for (UUID player : players) {
            List<Card> spells = trigger.filter().castFrom() == null ? gameData.getSpellsCastThisTurn(player)
                    : gameData.getSpellsCastThisTurn(player, trigger.filter().castFrom());
            matchingSpells += spells.stream()
                    .filter(spell -> predicateEvaluationService.matchesCardPredicate(
                            spell, trigger.filter().spell(), null, gameData, player))
                    .count();
        }
        return matchingSpells;
    }

    private StackEntry findStackEntryForCard(com.github.laxika.magicalvibes.model.GameData gameData, UUID cardId) {
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(cardId)) {
                return se;
            }
        }
        return null;
    }

    private int spellManaValue(com.github.laxika.magicalvibes.model.GameData gameData, Card spellCard) {
        StackEntry spellEntry = findStackEntryForCard(gameData, spellCard.getId());
        if (spellEntry != null && spellEntry.isCastFaceDown()) {
            return 0;
        }
        return spellCard.getManaValue() + (spellEntry == null || spellCard.getParsedManaCost() == null
                ? 0 : spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount());
    }

    /**
     * Collects {@code ability}, whose land-play trigger {@code trigger} narrows the land played by a filter or by the
     * zone it was played from, for the land play {@code landPlayed}: the trigger's filters and its event condition are
     * judged as the land is played, and the stack door judges its intervening "if" (CR 603.4; checked again as it
     * resolves) and limit and puts it on the stack as one object (CR 603.3), choosing its targets as it goes there
     * (CR 603.3d). The record names the player who played the land as the non-targeting "that player" and the land as
     * the triggering card ("that card", "that card's name"), whatever the effects are, with the source as it triggered
     * (what a transform instruction compares, CR 701.27f). The land played is the triggering card the event condition
     * and intervening "if" read ("if it shares a mana value"); one the card writes as an effect condition on the
     * triggering card ("if it shares a card type with the exiled card") is the card's own effect, judged as it resolves.
     *
     * @return whether the ability triggered
     */
    private boolean collectLandPlayAbility(TriggerMatchContext match, TriggeredAbility ability, LandPlayTrigger trigger,
                                          SpellEvent.LandPlayed landPlayed) {
        if (trigger.playedFrom() != null && landPlayed.playZone() != trigger.playedFrom()
                || trigger.notPlayedFrom() != null && landPlayed.playZone() == trigger.notPlayedFrom()) {
            return false;
        }
        // "A card exiled with this": the land was exiled with the ability's source, the permanent, the face-up plane or
        // the permanent that registered the ability, which the land-play event names since the land has left exile.
        if (trigger.exiledWithSource()) {
            UUID sourceId = match.exiledWithSourceId();
            if (sourceId == null || !sourceId.equals(landPlayed.exiledSourcePermanentId())) {
                return false;
            }
        }
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        UUID sourceCardId = match.permanent() != null ? match.permanent().getOriginalCard().getId()
                : match.sourceCard().getId();
        CardPredicate filter = trigger.filter();
        if (filter != null && !predicateEvaluationService.matchesCardPredicate(landPlayed.landCard(), filter,
                sourceCardId, match.gameData(), match.controllerId())) {
            return false;
        }
        ConditionContext context = (match.permanent() != null
                ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                : ConditionContext.forCard(sourceCard, match.controllerId()))
                .withTriggeringCard(landPlayed.landCard());
        if (!conditionEvaluationService.eventConditionMet(match.gameData(), ability.options(), context)) {
            return false;
        }
        Permanent source = match.permanent();
        PlanarObject planarObject = match.sourcePlanarObject();
        Card land = landPlayed.landCard();
        boolean triggered = triggeredAbilityStackService.put(match.gameData(), new TriggeredAbilityRequest(ability,
                sourceCard, source, match.controllerId(), sourceCard.getName() + "'s ability", context, entry -> {
                    entry.setTargetId(landPlayed.playingPlayerId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringCardId(land.getId());
                    entry.setTriggeringCards(List.of(land));
                    if (source != null) {
                        entry.setSourcePermanentSnapshot(new Permanent(source));
                    }
                    if (planarObject != null) {
                        entry.setSourcePlanarObject(planarObject);
                    }
                }));
        if (triggered) {
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }


    /**
     * {@code ability}'s land-play trigger condition that narrows {@code watched} by a filter or a zone, functioning in
     * {@code zone} (or in any zone when {@code anyZone}, as a granted ability is read), or {@code null} when the ability
     * has none: then it is read by its unfiltered trigger, if it has {@code watched}.
     */
    static LandPlayTrigger watchedLandPlayTrigger(TriggeredAbility ability, Trigger watched, Zone zone,
                                                  boolean anyZone) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof LandPlayTrigger landPlay && landPlay.filtered()
                    && landPlay.unfiltered().equals(watched)
                    && (anyZone || condition.zones().functionsIn(zone))) {
                return landPlay;
            }
        }
        return null;
    }

    /** Whether {@code ability} has a cast, copy or land-play trigger condition that narrows {@code watched}. */
    private static boolean readsItself(TriggeredAbility ability, Trigger watched, Zone zone, boolean anyZone) {
        return watchedSpellFilter(ability, watched, zone, anyZone) != null
                || watchedLandPlayTrigger(ability, watched, zone, anyZone) != null;
    }

    /**
     * The filter of {@code ability}'s cast or copy trigger condition that narrows {@code watched}, functioning in
     * {@code zone} (or in any zone when {@code anyZone}, as a granted ability is read), or {@code null} when it has none.
     */
    static SpellFilter watchedSpellFilter(TriggeredAbility ability, Trigger watched, Zone zone, boolean anyZone) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof SpellTrigger spell && spell.filter() != null
                    && spell.unfiltered().equals(watched)
                    && (anyZone || condition.zones().functionsIn(zone))) {
                return spell.filter();
            }
        }
        return null;
    }

    /**
     * The filter {@code ability} is judged with on {@code watched}: its trigger's filter ({@link #watchedSpellFilter}),
     * or {@link SpellFilter#ANY} for a cast trigger without one ("whenever an opponent casts a spell": any spell), so
     * the record names the spell, its caster and the cast event's value whatever the effects are; {@code null} when
     * neither applies.
     */
    private static SpellFilter collectedSpellFilter(TriggeredAbility ability, Trigger watched, Zone zone,
                                                    boolean anyZone) {
        SpellFilter filter = watchedSpellFilter(ability, watched, zone, anyZone);
        if (filter == null && watched instanceof SpellTrigger spell
                && spell.event() == SpellTrigger.SpellEvent.CAST
                && (anyZone ? ability.hasWatchedTrigger(watched) : ability.hasWatchedTrigger(watched, zone))) {
            return SpellFilter.ANY;
        }
        return filter;
    }

    /**
     * Triggers the abilities of {@code perm}, a permanent or a face-up scheme, on {@code watched} for {@code event}: the
     * abilities it has as the layers leave it ({@link TriggerIndex#permanentAbilities}), a printed one's spell filter
     * and effects read through the permanent's text changes (Layer 3, CR 612.1). A cast ability (with a spell filter or
     * none, "any spell") and a copy ability with a spell filter are judged by the cast and copy readers, one whose
     * land-play trigger has a filter or a zone by the land-play reader, a card-advantage ability by its own, and any
     * other with {@code watched} by {@link #collectUnfilteredAbility}.
     *
     * @return whether one triggered
     */
    private boolean dispatchSpellWatch(GameData gameData, Permanent perm, UUID controllerId, Trigger watched,
                                       SpellEvent event) {
        boolean triggered = false;
        Card card = perm.getCard();
        List<TriggeredAbility> printedAbilities = card.getTriggeredAbilities();
        List<TextReplacement> globalWordChange = null;
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, perm)) {
            boolean printed = printedAbilities.contains(ability);
            if (!readsItself(ability, watched, Zone.BATTLEFIELD, !printed)
                    && !(printed ? ability.hasWatchedTrigger(watched, Zone.BATTLEFIELD)
                    : ability.hasWatchedTrigger(watched))) {
                continue;
            }
            if (printed && globalWordChange == null) {
                globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            }
            List<TextReplacement> wordChange = globalWordChange;
            SpellFilter filter = collectedSpellFilter(ability, watched, Zone.BATTLEFIELD, !printed);
            if (filter != null) {
                if (printed) {
                    filter = TextChangeTransformer.transformSpellFilter(filter, perm.getTextReplacements(),
                            wordChange);
                }
                triggered |= collectSpellAbility(gameData, perm, card, controllerId, watched, event, ability,
                        filter, null);
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(ability, watched, Zone.BATTLEFIELD, !printed);
            if (landPlay != null) {
                triggered |= collectLandPlayAbility(gameData, perm, card, controllerId, event, ability, landPlay,
                        null);
                continue;
            }
            if (watched.equals(CARD_ADVANTAGE)) {
                triggered |= collectCardAdvantageAbility(new TriggerMatchContext(gameData, perm, controllerId,
                        List.of(), card), ability, (SpellEvent.CardAdvantage) event);
                continue;
            }
            // Layer 3 (CR 612.1): the trigger condition is read from the permanent's text-changed words.
            triggered |= collectUnfilteredAbility(gameData, perm, card, controllerId, event, ability,
                    printed ? effect -> TextChangeTransformer.transform(effect, perm.getTextReplacements(), wordChange)
                            : UnaryOperator.identity(), null);
        }
        return triggered;
    }

    /** Triggers the abilities of each face-up scheme in a command zone whose owner passes {@code ownerFilter} (CR 314.4). */
    private void dispatchFaceUpSchemes(GameData gameData, Trigger watched, SpellEvent event,
                                       Predicate<UUID> ownerFilter) {
        gameData.forEachFaceUpSchemeSource((ownerId, scheme) -> {
            if (ownerFilter.test(ownerId)) {
                dispatchSpellWatch(gameData, scheme, ownerId, watched, event);
            }
        });
    }

    /**
     * Triggers the abilities of {@code card}, which has no permanent (a card in exile, a face-up plane or phenomenon,
     * {@code planarObject}), on {@code watched} in {@code zone}: a filtered cast or land-play ability is judged by its
     * reader, any other with {@code watched} by {@link #collectUnfilteredAbility}.
     */
    private void dispatchCardSpellAbilities(GameData gameData, Card card, UUID controllerId, Trigger watched,
                                            Zone zone, SpellEvent event, PlanarObject planarObject) {
        for (TriggeredAbility ability : card.getTriggeredAbilities()) {
            SpellFilter filter = collectedSpellFilter(ability, watched, zone, false);
            if (filter != null) {
                collectSpellAbility(gameData, null, card, controllerId, watched, event, ability, filter,
                        planarObject);
                continue;
            }
            LandPlayTrigger landPlay = watchedLandPlayTrigger(ability, watched, zone, false);
            if (landPlay != null) {
                collectLandPlayAbility(gameData, null, card, controllerId, event, ability, landPlay, planarObject);
                continue;
            }
            if (!ability.hasWatchedTrigger(watched, zone)) {
                continue;
            }
            collectUnfilteredAbility(gameData, null, card, controllerId, event, ability, UnaryOperator.identity(),
                    planarObject);
        }
    }

    /**
     * Triggers {@code ability}, which has a spell, land-play or counter event's own trigger (no spell filter, land
     * filter or zone), for {@code event}: its event condition is judged as it triggers (CR 603.2), and the stack door
     * judges its intervening "if" and limit and puts it on the stack as one object of its effects (CR 603.3), read
     * through {@code transform} (the permanent's text changes), with the event recorded whatever they are
     * ({@link #spellWatchRecord}). {@code perm} is the source on the battlefield, or {@code null} for
     * {@code sourceCard} in exile or a face-up plane or phenomenon ({@code planarObject}).
     *
     * @return whether the ability triggered
     */
    private boolean collectUnfilteredAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                             SpellEvent event, TriggeredAbility ability,
                                             UnaryOperator<CardEffect> transform, PlanarObject planarObject) {
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No spell, land-play or activation reader reads "
                    + ability.options().multiplicity() + ": " + ability);
        }
        ConditionContext context = perm != null ? ConditionContext.forPermanent(perm, controllerId)
                : ConditionContext.forCard(sourceCard, controllerId);
        // A land-play ability's options read the land played as the triggering card ("if it shares a mana value").
        if (event instanceof SpellEvent.LandPlayed landPlayed) {
            context = context.withTriggeringCard(landPlayed.landCard());
        }
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = transform.apply(registration.effect());
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                changed ? ability.withEffects(transformed) : ability, sourceCard, perm, controllerId,
                sourceCard.getName() + "'s ability", context, spellWatchRecord(event, perm, planarObject))
                .withEvent(doublerEvent(event)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

    /**
     * The event the stack door's "triggers an additional time" effects read ("an instant or sorcery spell you cast or
     * copy", Veyran), or {@code null} for an event none of them names.
     */
    private static TriggerContext doublerEvent(SpellEvent event) {
        return switch (event) {
            case SpellEvent.Cast cast -> castContext(cast);
            case SpellEvent.Copied copied -> new TriggerContext.SpellCopy(copied.copy(), copied.copyingPlayerId());
            default -> null;
        };
    }

    private static TriggerContext.SpellCast castContext(SpellEvent.Cast cast) {
        return new TriggerContext.SpellCast(cast.spellCard(), cast.castingPlayerId(), cast.castZone(),
                cast.exiledSourcePermanentId());
    }

    /**
     * The record of a spell, land-play, counter or mana-ability event, stamped on the ability's entry whatever its
     * effects are: the player who played the land as the non-targeting "that player" and the land as the triggering
     * card ("that card"), the player who activated the mana ability with the mana produced as the event's value, and
     * the spell countered or fizzled as the triggering spell, as it last existed on the stack (CR 608.2h); with the
     * source as it triggered and the face-up plane or phenomenon whose ability it is.
     */
    private static TriggerRecord spellWatchRecord(SpellEvent event, Permanent perm, PlanarObject planarObject) {
        return entry -> {
            entry.setNonTargeting(true);
            if (perm != null) {
                entry.setSourcePermanentSnapshot(new Permanent(perm));
            }
            if (planarObject != null) {
                entry.setSourcePlanarObject(planarObject.copy());
            }
            switch (event) {
                case SpellEvent.LandPlayed landPlayed -> {
                    entry.setTargetId(landPlayed.playingPlayerId());
                    entry.setTriggeringCardId(landPlayed.landCard().getId());
                    entry.setTriggeringCards(List.of(landPlayed.landCard()));
                }
                case SpellEvent.ManaAbilityResolved mana -> {
                    entry.setTriggeringPlayerId(mana.activatingPlayerId());
                    entry.setEventValue(mana.manaProduced());
                }
                case SpellEvent.CounteredOrFizzled countered -> {
                    entry.setTriggeringCardId(countered.spell().getCard().getId());
                    entry.setTriggeringSpell(countered.spell());
                }
                default -> {
                }
            }
        };
    }

    /**
     * Triggers {@code ability}, whose cast or copy trigger has {@code filter}, for the cast or copy {@code event}, through
     * the cast or copy reader, which judges the filter and hands the ability to the stack door. {@code perm} is its
     * source on the battlefield (as it last existed, for a mana ability's "when you spend this mana" rider), or
     * {@code null} for {@code sourceCard} elsewhere; {@code planarObject} is the face-up plane or phenomenon whose
     * ability it is, or {@code null}.
     *
     * @return whether the ability triggered
     */
    private boolean collectSpellAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                        Trigger watched, SpellEvent event, TriggeredAbility ability,
                                        SpellFilter filter, PlanarObject planarObject) {
        TriggerMatchContext match = new TriggerMatchContext(gameData, perm, controllerId, List.of(), sourceCard,
                planarObject == null ? null : planarObject.copy());
        if (event instanceof SpellEvent.Cast cast) {
            return collectCastAbility(match, CastAbility.of(ability, filter), cast, watched.equals(YOU_CAST));
        }
        return collectCopyAbility(match, ability, filter, (SpellEvent.Copied) event);
    }

    /**
     * Triggers {@code ability}, whose land-play trigger {@code trigger} has a filter or a zone, for the land play
     * {@code event}, through the land-play reader ({@link #collectLandPlayAbility(TriggerMatchContext,
     * TriggeredAbility, LandPlayTrigger, SpellEvent.LandPlayed)}).
     *
     * @return whether the ability triggered
     */
    private boolean collectLandPlayAbility(GameData gameData, Permanent perm, Card sourceCard, UUID controllerId,
                                           SpellEvent event, TriggeredAbility ability, LandPlayTrigger trigger,
                                           PlanarObject planarObject) {
        return collectLandPlayAbility(new TriggerMatchContext(gameData, perm, controllerId, List.of(), sourceCard,
                        planarObject == null ? null : planarObject.copy()),
                ability, trigger, (SpellEvent.LandPlayed) event);
    }

    /**
     * The effects of {@code watcher}'s ability, every one of them as written (CR 603.3), its intervening "if" wrapped
     * around each to be judged again as it resolves (CR 603.4).
     */
    private static List<CardEffect> temporaryEffects(TemporaryGlobalTriggeredAbility watcher) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        return watcher.ability().effects().stream()
                .map(EffectRegistration::effect)
                .map(effect -> interveningIf == null ? effect : (CardEffect) new ConditionalEffect(interveningIf, effect))
                .toList();
    }

    /**
     * The match context of an ability a resolving ability registered: no permanent of its own, its source card the
     * registering card, and the permanent that registered it as its "this" (CR 603.7e).
     */
    private static TriggerMatchContext registeredMatch(GameData gameData, TemporaryGlobalTriggeredAbility watcher) {
        return new TriggerMatchContext(gameData, null, watcher.controllerId(), temporaryEffects(watcher),
                watcher.sourceCard(), null, null, false, watcher.sourcePermanentId());
    }

    private static void requireNoLimit(TriggeredAbility ability, Card card) {
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has a limit its reader doesn't "
                    + "judge: " + ability.options());
        }
    }

    /**
     * The graveyard, hand and command-zone cast readers judge no limit, and the hand and command-zone readers and the
     * graveyard exhaust reader no intervening "if" either; a card with one there fails loudly instead of triggering
     * wrongly.
     */
    private static void requireNoOptions(TriggeredAbility ability, Card card) {
        if (!ability.options().equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has options its zone's reader "
                    + "doesn't judge: " + ability.options());
        }
    }

    /** The filter context of a predicate {@code watcher}, a permanent with a triggered ability, judges. */
    private static FilterContext watcherFilterContext(GameData gameData, Permanent watcher, UUID watcherControllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
    }

    /**
     * Records {@code spellEntry}, the spell just cast, as the triggering spell of {@code trigger}, one of the abilities
     * its casting triggered that refers to it ("copy it", "copy this spell"): as it is now, kept as it last existed on
     * the stack once it has left (CR 608.2h), with its places among its caster's spells and every player's spells
     * this turn, and the cast facts its copy counts and intervening "if"s read (its X, repeatable additional cost
     * payments and casualty payment).
     * The ability doesn't target the spell.
     */
    public static void recordTriggeringSpell(GameData gameData, StackEntry trigger, StackEntry spellEntry) {
        UUID spellId = spellEntry.getCard().getId();
        trigger.setTriggeringCardId(spellId);
        trigger.setTriggeringSpell(spellEntry);
        trigger.setTriggeringSpellCastIndex(gameData.getSpellCastIndexThisTurn(spellEntry.getControllerId(), spellId));
        trigger.setTriggeringSpellTurnCastIndex(gameData.getSpellCastOrdinalThisTurn(spellId) - 1);
        trigger.setXValue(spellEntry.getXValue());
        trigger.setRepeatedAdditionalCosts(List.copyOf(spellEntry.getRepeatedAdditionalCosts()));
        trigger.setCasualtyCostPaid(spellEntry.isCasualtyCostPaid());
        trigger.setNonTargeting(true);
    }

    private static StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
