package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TriggeredManaAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.rules.TapEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TapTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerJudge;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triggers the tap and untap abilities of a {@link TapEvent} (CR 603.2): "whenever [a permanent] becomes tapped /
 * untapped", "whenever you tap a permanent an opponent controls", the untap-step and teamwork abilities, and the
 * tapped-for-mana abilities (CR 106.12). The abilities are read from {@link TriggerIndex} as the layers leave them and
 * matched by their {@link TapTrigger}: its filter on the permanent, its tap relation ("you tap", "an opponent taps",
 * "enchanted land", "this land"), "for the first time during your turn" and "one or more" are judged here, the
 * intervening "if" (CR 603.4) and limit (CR 603.2h) by the door. Each is handed whole to the stack door with the record
 * of the tap, but a triggered mana ability (CR 605.1b), which resolves at once (CR 605.4a).
 */
@Slf4j
@Service
public class TapTriggers {

    /** The trigger of a tapped-for-mana ability as the door gets it: a permanent tapped for mana. */
    private static final TapTrigger TAPPED_FOR_MANA = Triggers.tappedForMana();
    private static final Trigger PLAYER_TAPS_LAND = Triggers.playerTapsLandForMana();
    private static final Trigger PLAYER_TAPS_PERMANENT = Triggers.playerTapsPermanentForMana();
    private static final Trigger YOU_TAP_CREATURE = Triggers.youTapCreatureForMana();
    private static final Trigger YOU_TAP_NONLAND_PERMANENT = Triggers.youTapNonlandPermanentForMana();
    private static final Trigger YOU_TAP_ARTIFACT_TOKEN = Triggers.youTapArtifactTokenForMana();
    private static final Trigger ENCHANTED_PERMANENT_TAPPED = Triggers.enchantedPermanentBecomesTapped();
    private static final Trigger PERMANENT_YOU_CONTROL_TAPPED = Triggers.permanentYouControlBecomesTapped();
    private static final Trigger PERMANENT_OPPONENT_CONTROLS_TAPPED = Triggers.permanentOpponentControlsBecomesTapped();
    private static final Trigger YOU_TAP_OPPONENT_PERMANENT = Triggers.youTapPermanentOpponentControls();
    private static final Trigger CREATURE_TAPPED = Triggers.creatureBecomesTapped();
    private static final Trigger TEAMWORK = Triggers.tappedToPayTeamworkCost();
    private static final Trigger SELF_UNTAPPED = Triggers.becomesUntapped();
    private static final Trigger PERMANENT_YOU_CONTROL_UNTAPPED = Triggers.permanentYouControlBecomesUntapped();
    private static final Trigger PERMANENT_UNTAPPED = Triggers.permanentBecomesUntapped();
    private static final Trigger UNTAP_STEP = Triggers.youUntapPermanentsDuringUntapStep();

    private final EventAbilitySupport abilities;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final TriggeredAbilityStackService stackService;
    private final TriggerCollectionService triggerCollectionService;

    public TapTriggers(EventAbilitySupport abilities, @Lazy GameQueryService gameQueryService,
                       @Lazy PredicateEvaluationService predicateEvaluationService,
                       @Lazy ConditionEvaluationService conditionEvaluationService,
                       @Lazy GameLogService gameLogService, @Lazy GameOutcomeService gameOutcomeService,
                       @Lazy EffectHandlerRegistry effectHandlerRegistry,
                       @Lazy TriggeredAbilityStackService stackService,
                       @Lazy TriggerCollectionService triggerCollectionService) {
        this.abilities = abilities;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.gameOutcomeService = gameOutcomeService;
        this.effectHandlerRegistry = effectHandlerRegistry;
        this.stackService = stackService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, TapEvent event) {
        switch (event) {
            case TapEvent.BecameTapped tapped -> becameTapped(gameData, tapped);
            case TapEvent.BecameUntapped untapped -> becameUntapped(gameData, untapped.permanent());
            case TapEvent.UntappedDuringUntapStep step -> untapStep(gameData, step.controllerId(),
                    step.untappedCount());
            case TapEvent.TappedForTeamwork teamwork -> tappedForTeamwork(gameData, teamwork.creature());
            case TapEvent.SelfTappedForMana self -> selfTappedForMana(gameData, self.permanent(), self.controllerId());
            case TapEvent.LandTappedForMana land -> landTapped(gameData, land.tappingPlayerId(), land.landId(),
                    land.producedColors());
            case TapEvent.CreatureTappedForMana creature -> creatureTapped(gameData, creature.tappingPlayerId(),
                    creature.creatureId());
            case TapEvent.PermanentTappedForMana permanent -> {
                if (permanent.anyPermanent()) {
                    anyPlayerTapsPermanent(gameData, permanent);
                } else {
                    nonlandPermanentTapped(gameData, permanent);
                }
            }
        }
    }

    /**
     * "Whenever you tap this permanent for mana" (Zhur-Taa Druid): only the tapped permanent's abilities, and only the
     * mana-ability tap path emits the event, so tapping to attack or an opponent's forced tap never triggers it. Each
     * goes to the stack door whole (CR 603.3), the permanent its triggering permanent.
     */
    private void selfTappedForMana(GameData gameData, Permanent tappedPermanent, UUID controllerId) {
        for (TriggeredAbility ability : abilities.abilities(gameData, tappedPermanent, TAPPED_FOR_MANA)) {
            requireNothingToJudge(read(ability, TAPPED_FOR_MANA).getFirst(), TAPPED_FOR_MANA);
            abilities.put(gameData, ability, tappedPermanent.getCard(), tappedPermanent, controllerId,
                    ConditionContext.forPermanent(tappedPermanent, controllerId),
                    entry -> entry.setTriggeringPermanentId(tappedPermanent.getId()));
        }
    }

    /**
     * "Whenever a player taps a land for mana" and the abilities read by that trigger (Manabarbs, Mana Flare, Wild
     * Growth, Price of Glory): the player who tapped the land is recorded as having done so this turn even when nothing
     * triggers (Desolation, 2004-10-04 ruling); the battlefields' abilities, the face-up planes', a player's floating
     * "whenever you tap a land for mana" (Selesnya Loft Gardens' chaos) and the turn-scoped extra mana of Chaos Moon
     * follow, in that order.
     */
    private void landTapped(GameData gameData, UUID tappingPlayerId, UUID tappedLandId,
                            Set<ManaColor> producedColors) {
        gameData.playersWhoTappedLandForManaThisTurn.add(tappingPlayerId);
        boolean[] anyTriggered = {false};
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                // "That player" of an intervening "if" ("if it's not that player's turn": Price of Glory) is the
                // player who tapped the land, the entry's non-targeting target as it resolves.
                TriggerJudge judge = conditionEvaluationService.judge(gameData,
                        ConditionContext.forPermanent(perm, playerId).withTargetId(tappingPlayerId));
                for (List<TapRead> reads : readAsJudged(abilities.abilities(gameData, perm, PLAYER_TAPS_LAND),
                        PLAYER_TAPS_LAND, judge)) {
                    if (!tapForManaMatches(gameData, reads.getFirst(), perm, perm.getCard(), playerId,
                            tappingPlayerId, tappedLandId)) {
                        continue;
                    }
                    if (collect(gameData, new Source(perm, playerId, effectsOf(reads), perm.getCard(), null),
                            tappingPlayerId, tappedLandId, producedColors, tappingPlayerId)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                TriggerJudge judge = conditionEvaluationService.judge(gameData,
                        ConditionContext.forCard(object.getCard(), planarControllerId).withTargetId(tappingPlayerId));
                for (List<TapRead> reads : readAsJudged(EventAbilitySupport.planarAbilities(object.getCard(),
                        PLAYER_TAPS_LAND), PLAYER_TAPS_LAND, judge)) {
                    if (!tapForManaMatches(gameData, reads.getFirst(), null, object.getCard(), planarControllerId,
                            tappingPlayerId, tappedLandId)) {
                        continue;
                    }
                    if (collect(gameData, new Source(null, planarControllerId, effectsOf(reads), object.getCard(),
                            object.copy()), tappingPlayerId, tappedLandId, producedColors, tappingPlayerId)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : List.copyOf(gameData.floatingEffects)) {
                if (!(floating.effect() instanceof AddOneOfEachManaTypeProducedByLandEffect effect)
                        || floating.affectedPlayerId() == null
                        || !floating.affectedPlayerId().equals(tappingPlayerId)) {
                    continue;
                }
                if (collect(gameData, new Source(null, floating.affectedPlayerId(), List.of(effect), null, null),
                        tappingPlayerId, tappedLandId, producedColors, tappingPlayerId)) {
                    anyTriggered[0] = true;
                }
            }
        }
        applyTurnScopedExtraLandTapMana(gameData, tappingPlayerId, tappedLandId);
        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
        // A targeted ability of the tap chooses its target as it goes on the stack (CR 603.3d), right after the tap.
        triggerCollectionService.beginStackDoorChoices(gameData);
    }

    /** "Whenever you tap a creature for mana" (Badgermole Cub). */
    private void creatureTapped(GameData gameData, UUID tappingPlayerId, UUID tappedCreatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;
        for (Permanent perm : List.copyOf(battlefield)) {
            for (List<CardEffect> effects : strictEffects(abilities.abilities(gameData, perm, YOU_TAP_CREATURE),
                    YOU_TAP_CREATURE)) {
                collect(gameData, new Source(perm, tappingPlayerId, effects, perm.getCard(), null), tappingPlayerId,
                        tappedCreatureId, Set.of(), tappedCreatureId, true);
            }
        }
        triggerCollectionService.beginStackDoorChoices(gameData);
    }

    /** "Whenever you tap a nonland permanent for mana" (Kinnan) and "an artifact token" (Roxanne). */
    private void nonlandPermanentTapped(GameData gameData, TapEvent.PermanentTappedForMana event) {
        Permanent tappedPermanent = event.permanent();
        UUID tappingPlayerId = event.tappingPlayerId();
        if (tappedPermanent.getCard().hasType(CardType.LAND)) {
            return;
        }
        Set<ManaColor> producedManaTypes = event.manaTypeChoicePending() ? null
                : newlyProducedManaTypes(event.manaBefore(),
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());
        if (producedManaTypes != null && producedManaTypes.isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;
        List<Trigger> watched = tappedPermanent.getCard().isToken() && tappedPermanent.getCard().hasType(CardType.ARTIFACT)
                ? List.of(YOU_TAP_NONLAND_PERMANENT, YOU_TAP_ARTIFACT_TOKEN) : List.of(YOU_TAP_NONLAND_PERMANENT);
        for (Trigger trigger : watched) {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (List<CardEffect> effects : strictEffects(abilities.abilities(gameData, perm, trigger), trigger)) {
                    collect(gameData, new Source(perm, tappingPlayerId, effects, perm.getCard(), null),
                            tappingPlayerId, tappedPermanent.getId(), producedManaTypes, tappedPermanent.getId(),
                            true);
                }
            }
        }
        triggerCollectionService.beginStackDoorChoices(gameData);
    }

    /**
     * "Whenever a player / an opponent taps a permanent for mana" (Eloren Wilds, Treasure Nabber), of every permanent
     * and the face-up planes: the tap relation and the filter on the tapped permanent ("an opponent taps an artifact")
     * are judged, as are the abilities' intervening "if" and event condition as they trigger.
     */
    private void anyPlayerTapsPermanent(GameData gameData, TapEvent.PermanentTappedForMana event) {
        UUID tappingPlayerId = event.tappingPlayerId();
        Permanent tappedPermanent = gameQueryService.findPermanentById(gameData, event.permanent().getId());
        if (tappedPermanent == null) {
            return;
        }
        UUID tappedPermanentId = tappedPermanent.getId();
        Set<ManaColor> producedManaTypes = event.manaTypeChoicePending() ? null
                : newlyProducedManaTypes(event.manaBefore(),
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());
        gameData.forEachPermanent((controllerId, permanent) -> {
            TriggerJudge judge = conditionEvaluationService.judge(gameData,
                    ConditionContext.forPermanent(permanent, controllerId));
            for (List<TapRead> reads : readAsJudged(abilities.abilities(gameData, permanent, PLAYER_TAPS_PERMANENT),
                    PLAYER_TAPS_PERMANENT, judge)) {
                if (!tapForManaMatches(gameData, reads.getFirst(), permanent, permanent.getCard(), controllerId,
                        tappingPlayerId, tappedPermanentId)) {
                    continue;
                }
                collect(gameData, new Source(permanent, controllerId, effectsOf(reads), permanent.getCard(), null),
                        tappingPlayerId, tappedPermanentId, producedManaTypes, tappedPermanentId, true);
            }
        });
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (List<CardEffect> effects : strictEffects(EventAbilitySupport.planarAbilities(object.getCard(),
                        PLAYER_TAPS_PERMANENT), PLAYER_TAPS_PERMANENT)) {
                    collect(gameData, new Source(null, gameData.planechase.controllerId, effects, object.getCard(),
                                    object.copy()), tappingPlayerId, tappedPermanentId, producedManaTypes,
                            tappedPermanentId, true);
                }
            }
        }
        triggerCollectionService.beginStackDoorChoices(gameData);
    }

    /**
     * Whether the tap for mana matches {@code read}'s trigger conditions, as the permanent is tapped: the tap relation
     * ("you tap" and "an opponent taps", the tapping player against the ability's controller; "enchanted land", the
     * permanent {@code source} is attached to; "this land", {@code source} itself) and the filter on the tapped
     * permanent ("an Island", "a nonbasic land", "an artifact"), judged on the permanent on the battlefield with the
     * ability's source as the filter's source. A permanent that left the battlefield as its mana ability resolved
     * (Gemstone Mine's last use) matches no filter: the objects that exist immediately after the event are checked
     * (CR 603.10).
     */
    private boolean tapForManaMatches(GameData gameData, TapRead read, Permanent source, Card sourceCard,
                                      UUID controllerId, UUID tappingPlayerId, UUID tappedId) {
        boolean relationMatches = switch (read.tapRelation()) {
            case YOU_CONTROL -> controllerId.equals(tappingPlayerId);
            case OPPONENT_CONTROLS -> !controllerId.equals(tappingPlayerId);
            case ENCHANTED -> source != null && source.isAttached() && tappedId.equals(source.getAttachedTo());
            case THIS -> source != null && source.getId().equals(tappedId);
            default -> true;
        };
        if (!relationMatches) {
            return false;
        }
        if (read.filter() == null) {
            return true;
        }
        Permanent tapped = gameQueryService.findPermanentById(gameData, tappedId);
        return tapped != null && predicateEvaluationService.matchesPermanentPredicate(tapped, read.filter(),
                FilterContext.of(gameData).withSourceCardId(sourceCard.getId()).withSourceControllerId(controllerId));
    }

    /**
     * Applies {@code GameData.extraManaOnLandSubtypeTapThisTurn}: a turn-scoped, symmetric "whenever a player taps a
     * land of this subtype for mana, that player adds an additional {X}" granted by a resolved effect (Chaos Moon's odd
     * branch). It is a triggered mana ability, so it pays straight into the tapping player's pool (CR 605.4a).
     */
    private void applyTurnScopedExtraLandTapMana(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        if (gameData.extraManaOnLandSubtypeTapThisTurn.isEmpty()) {
            return;
        }
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand == null) {
            return;
        }
        Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, tappedLand);
        ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
        if (pool == null) {
            return;
        }
        for (var entry : gameData.extraManaOnLandSubtypeTapThisTurn.entrySet()) {
            if (!types.contains(entry.getKey())) {
                continue;
            }
            for (var bonus : entry.getValue()) {
                ManaColor color = bonus.color() != ManaColor.COLORLESS
                        && gameData.playersWithColoredManaReplacementThisTurn.contains(bonus.controllerId())
                        ? ManaColor.WHITE : bonus.color();
                pool.add(color);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(tappingPlayerId) + " adds 1 additional "
                                + color.getCode() + " mana.")
                        .build());
            }
        }
    }

    private static Set<ManaColor> newlyProducedManaTypes(Map<ManaColor, Integer> before,
                                                         Map<ManaColor, Integer> after) {
        EnumSet<ManaColor> produced = EnumSet.noneOf(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (after.getOrDefault(color, 0) > before.getOrDefault(color, 0)) {
                produced.add(color);
            }
        }
        return produced;
    }

    /**
     * The source of a tapped-for-mana ability: a permanent, a face-up plane ({@code planarObject}), or a player's
     * granted ability with no source card.
     */
    record Source(Permanent permanent, UUID controllerId, List<CardEffect> effects, Card sourceCard,
                          PlanarObject planarObject) {

        boolean admits(TargetPredicate.Kind kind) {
            return effects.stream().anyMatch(effect -> effect.targetSpec().admits(kind));
        }
    }

    /** {@link #collect(GameData, Source, UUID, UUID, Set, UUID, boolean)} of a land tap. */
    private boolean collect(GameData gameData, Source source, UUID tappingPlayerId, UUID tappedPermanentId,
                            Set<ManaColor> producedColors, UUID thatObjectId) {
        return collect(gameData, source, tappingPlayerId, tappedPermanentId, producedColors, thatObjectId, false);
    }

    /**
     * Hands a tapped-for-mana ability to the stack door with a record of the tap ({@link #tapRequest}), as
     * <ul>
     *   <li>a triggered mana ability (no target, could add mana, CR 605.1b), resolved at once through its effects'
     *       handlers (CR 605.4a, {@link TriggeredAbilityStackService#putManaAbility}), or held until the tapped
     *       permanent's own mana choice settles the type of mana it produced when an effect reads it
     *       ({@link TriggeredAbilityStackService#holdForProducedMana});</li>
     *   <li>a targeted ability, whose target is chosen as it goes on the stack (CR 603.3d), after that mana choice when
     *       the produced mana isn't known yet;</li>
     *   <li>any other ability, put on the stack as written (it can't add mana, so it isn't a mana ability and uses the
     *       stack, CR 605.1b).</li>
     * </ul>
     * A permanent's tap ({@code permanentTap}) skips an ability that reads the produced mana when the tap produced
     * none.
     */
    boolean collect(GameData gameData, Source source, UUID tappingPlayerId, UUID tappedPermanentId,
                    Set<ManaColor> producedColors, UUID thatObjectId, boolean permanentTap) {
        if (permanentTap && producedColors != null && producedColors.isEmpty()
                && source.effects().stream().anyMatch(TapTriggers::readsProducedMana)) {
            return false;
        }
        boolean producedUnknown = producedColors == null || (producedColors.isEmpty()
                && gameData.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice);
        if (isTriggeredManaAbility(source.effects())) {
            if (producedUnknown && steps(source.effects()).stream().anyMatch(TapTriggers::readsProducedMana)) {
                return hold(gameData, source, tappingPlayerId, tappedPermanentId, thatObjectId);
            }
            return stackService.putManaAbility(gameData, tapRequest(gameData, source, tappingPlayerId,
                    tappedPermanentId, thatObjectId, producedColors == null ? Set.of() : producedColors),
                    entry -> resolveAtOnce(gameData, entry));
        }
        if (producedUnknown && (source.admits(TargetPredicate.Kind.PERMANENT)
                || source.admits(TargetPredicate.Kind.PLAYER))) {
            return hold(gameData, source, tappingPlayerId, tappedPermanentId, thatObjectId);
        }
        TriggeredAbilityRequest request = tapRequest(gameData, source, tappingPlayerId, tappedPermanentId,
                thatObjectId, producedColors == null ? Set.of() : producedColors);
        boolean triggered = stackService.put(gameData, request);
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(request.sourceCard()));
            log.info("Game {} - {} triggers on a tap for mana by {}", gameData.id, request.sourceCard().getName(),
                    gameData.playerIdToName.get(tappingPlayerId));
        }
        return triggered;
    }

    /**
     * Holds the ability at the stack door until the tapped permanent's own mana choice settles the produced mana
     * ({@code ChoiceHandlerService.resolveProducedManaTriggers} resolves a triggered mana ability then, and
     * {@link #putResolvedProducedManaTrigger} puts a targeted ability on the stack).
     */
    private boolean hold(GameData gameData, Source source, UUID tappingPlayerId, UUID tappedPermanentId,
                         UUID thatObjectId) {
        TriggeredAbilityRequest request = tapRequest(gameData, source, tappingPlayerId, tappedPermanentId,
                thatObjectId, null);
        boolean triggered = stackService.holdForProducedMana(gameData, request);
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(request.sourceCard()));
        }
        return triggered;
    }

    /**
     * The request of a tapped-for-mana ability, its effects as judged: its record names the tapping player as its
     * triggering player, the tapped permanent as its triggering permanent with its last-known information,
     * {@code thatObjectId} as its non-targeting target ("that player" of a land tap, "that permanent" of a
     * permanent's), the mana the tap produced ({@code null} while it isn't known), and the source as it was when the
     * ability triggered (a permanent, or a face-up plane). A triggered mana ability is its steps (a sequence's, each
     * resolved by its handler). A player's granted ability has no source card: its source is a placeholder named for
     * a mana ability.
     */
    private TriggeredAbilityRequest tapRequest(GameData gameData, Source source, UUID tappingPlayerId,
                                               UUID tappedPermanentId, UUID thatObjectId,
                                               Set<ManaColor> producedColors) {
        Card sourceCard = source.sourceCard() != null ? source.sourceCard()
                : source.permanent() != null ? source.permanent().getCard()
                : Card.namedRuntimePlaceholder("Mana ability");
        List<CardEffect> effects = isTriggeredManaAbility(source.effects())
                ? steps(source.effects()) : source.effects();
        Permanent tapped = tappedAsItLastExisted(gameData, tappedPermanentId);
        Permanent sourceSnapshot = source.permanent() == null ? null : new Permanent(source.permanent());
        PlanarObject planarObject = source.planarObject() == null ? null : source.planarObject().copy();
        TriggerRecord record = entry -> {
            entry.setTargetId(thatObjectId);
            entry.setNonTargeting(true);
            entry.setTriggeringPlayerId(tappingPlayerId);
            entry.setTriggeringPermanentId(tappedPermanentId);
            entry.setTriggeringPermanent(tapped);
            entry.setOnceEachTurnActionKey(null);
            if (sourceSnapshot != null) {
                entry.setSourcePermanentSnapshot(sourceSnapshot);
            }
            if (planarObject != null) {
                entry.setSourcePlanarObject(planarObject.copy());
            }
            if (producedColors != null) {
                entry.setProducedManaColors(producedColors);
            }
        };
        String description = source.sourceCard() == null && source.permanent() == null ? "Mana ability"
                : sourceCard.getName() + "'s ability";
        ConditionContext context = source.permanent() != null
                ? ConditionContext.forPermanent(source.permanent(), source.controllerId())
                : ConditionContext.forCard(sourceCard, source.controllerId());
        return new TriggeredAbilityRequest(new TriggeredAbility(TAPPED_FOR_MANA,
                effects.toArray(CardEffect[]::new)), sourceCard, source.permanent(), source.controllerId(),
                description, context, record);
    }

    /**
     * Resolves the triggered mana ability {@code entry} at once (CR 605.4a), each step by its handler, reading the tap
     * from the entry.
     */
    private void resolveAtOnce(GameData gameData, StackEntry entry) {
        // A step that waits for a choice takes the steps after it into a follow-up, so the entry is read afresh.
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect effect = entry.getEffectsToResolve().get(i);
            EffectHandler handler = effectHandlerRegistry.getHandler(effect);
            if (handler == null) {
                log.warn("No handler for triggered mana ability effect: {}", effect.getClass().getSimpleName());
                continue;
            }
            entry.setResolvingEffectIndex(i);
            handler.resolve(gameData, entry, effect);
        }
        entry.setResolvingEffectIndex(-1);
    }

    /** Whether {@code effect} is a triggered mana ability's effect that reads the type of mana the tap produced. */
    private static boolean readsProducedMana(CardEffect effect) {
        return effect instanceof TriggeredManaAbilityEffect mana && mana.readsProducedMana();
    }

    /**
     * The tapped permanent as it is on the battlefield, or as it last existed there once its mana ability made it leave
     * (Gemstone Mine's last use, a sacrificed Treasure), for "that land" (CR 608.2h); {@code null} when neither is known.
     */
    private Permanent tappedAsItLastExisted(GameData gameData, UUID tappedPermanentId) {
        Permanent tapped = gameQueryService.findPermanentById(gameData, tappedPermanentId);
        if (tapped != null) {
            return tapped;
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> snapshot.getId().equals(tappedPermanentId))
                .findFirst().orElse(null);
    }

    /**
     * Puts a held targeted ability (C.A.M.P.: it targets, so it isn't a mana ability, CR 605.1b) on the stack once the
     * tapped permanent's mana choice settled {@code manaColor}: its target is chosen as it goes there (CR 603.3d), with
     * its record of the tap. {@code false} for a held triggered mana ability, which the caller resolves.
     */
    public boolean putResolvedProducedManaTrigger(GameData gameData, StackEntry entry, ManaColor manaColor) {
        if (entry.getEffectsToResolve().stream().noneMatch(effect -> effect.targetSpec().admits(
                TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            return false;
        }
        entry.setProducedManaColors(Set.of(manaColor));
        stackService.putHeld(gameData, entry);
        if (!gameData.interaction.isAwaitingInput()) {
            triggerCollectionService.beginStackDoorChoices(gameData);
        }
        return true;
    }

    /**
     * {@code event.permanent()} became tapped: the creature's taps this turn are counted (for "for the first time
     * during each of your turns"); then the delayed "whenever a creature becomes tapped" abilities, the emblems', the
     * Auras' "whenever enchanted permanent becomes tapped", the tapped permanent's controller's "whenever a permanent
     * you control becomes tapped", the other players' "whenever a permanent an opponent controls becomes tapped", the
     * delayed ones of both, and the tapping player's "whenever you tap a permanent an opponent controls".
     */
    private void becameTapped(GameData gameData, TapEvent.BecameTapped event) {
        Permanent tappedPermanent = event.permanent();
        UUID tappingPlayerId = event.tappingPlayerId();
        if (event.tappedByResolvingController()) {
            tappingPlayerId = gameData.currentlyResolvingControllerId;
            if (tappingPlayerId == null && gameData.pendingEffectResolutionEntry != null) {
                tappingPlayerId = gameData.pendingEffectResolutionEntry.getControllerId();
            }
        }
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        int creatureTapCount = gameQueryService.isCreature(gameData, tappedPermanent)
                ? gameData.creatureTapCountsThisTurn.merge(tappedPermanent.getId(), 1, Integer::sum)
                : 0;
        if (creatureTapCount > 0) {
            temporaryTapped(gameData, CREATURE_TAPPED, tappedPermanent, null);
        }

        UUID controllerId = tappedPermanentControllerId;
        emblemTapped(gameData, tappedPermanent, controllerId);

        // "Whenever enchanted permanent becomes tapped": the ability is the Aura's (CR 113.7); the tapped permanent's
        // controller is the non-targeting "that player" ("that player mills three cards"), and a "choose one" (Relic
        // Bind) announces its mode, and a mode its target, as it goes on the stack (CR 603.3c).
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, ENCHANTED_PERMANENT_TAPPED)) {
                requireNothingToJudge(read(ability, ENCHANTED_PERMANENT_TAPPED).getFirst(),
                        ENCHANTED_PERMANENT_TAPPED);
                abilities.put(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId)
                                .withTriggeringPermanentId(tappedPermanent.getId()),
                        tappedRecord(perm, tappedPermanent, controllerId, controllerId));
            }
        });

        // "Whenever a permanent you control becomes tapped" (Judge of Currents).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, PERMANENT_YOU_CONTROL_TAPPED)) {
                TapRead read = read(ability, PERMANENT_YOU_CONTROL_TAPPED).getFirst();
                // "For the first time during each of your turns": the creature's first tap this turn, during its
                // controller's turn, counting its taps before the source was on the battlefield.
                if (read.firstTime() && (!controllerId.equals(gameData.activePlayerId) || creatureTapCount != 1)) {
                    continue;
                }
                putTapAbility(gameData, perm, ownerId, read, tappedPermanent, controllerId);
            }
        });

        // "Whenever a permanent an opponent controls becomes tapped" (Thoughtleech).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, PERMANENT_OPPONENT_CONTROLS_TAPPED)) {
                putTapAbility(gameData, perm, ownerId, read(ability, PERMANENT_OPPONENT_CONTROLS_TAPPED).getFirst(),
                        tappedPermanent, controllerId);
            }
        });

        temporaryTapped(gameData, PERMANENT_YOU_CONTROL_TAPPED, tappedPermanent, controllerId);
        temporaryTapped(gameData, PERMANENT_OPPONENT_CONTROLS_TAPPED, tappedPermanent, controllerId);

        // "Whenever you tap an untapped creature an opponent controls": the tapping player is tracked apart from the
        // tapped permanent's controller, as an effect may instruct another player to tap their own permanent.
        if (tappingPlayerId == null || tappingPlayerId.equals(controllerId)) {
            return;
        }
        UUID tapper = tappingPlayerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(tapper)) return;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, YOU_TAP_OPPONENT_PERMANENT)) {
                putTapAbility(gameData, perm, ownerId, read(ability, YOU_TAP_OPPONENT_PERMANENT).getFirst(),
                        tappedPermanent, controllerId);
            }
        });
    }

    /**
     * The delayed "whenever a creature becomes tapped" and "whenever a permanent you control / an opponent controls
     * becomes tapped" abilities with a stated duration (CR 603.7b) for {@code tapped}: "you control" and "an opponent
     * controls" are judged against {@code tappedControllerId}, a filter on the tapped permanent ("a creature an
     * opponent controls") as the ability's; the tapped permanent is the triggering permanent and the non-targeting
     * object it acts on. They are read with no "first time", "one or more" or limit.
     */
    private void temporaryTapped(GameData gameData, Trigger watched, Permanent tapped, UUID tappedControllerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(watched) || watcher.watchedPermanentId() != null) {
                continue;
            }
            if (watched == PERMANENT_YOU_CONTROL_TAPPED
                    && (tappedControllerId == null || !watcher.controllerId().equals(tappedControllerId))) {
                continue;
            }
            if (watched == PERMANENT_OPPONENT_CONTROLS_TAPPED
                    && (tappedControllerId == null || watcher.controllerId().equals(tappedControllerId))) {
                continue;
            }
            TapRead read = read(watcher.ability(), watched).getFirst();
            if (read.firstTime() || read.oneOrMore() || read.limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("No delayed tap ability reads a first time, \"one or more\" or a "
                        + "limit: " + watcher.ability());
            }
            if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(tapped, read.filter(),
                    FilterContext.of(gameData).withSourceCardId(watcher.sourceCard().getId())
                            .withSourceControllerId(watcher.controllerId()))) {
                continue;
            }
            abilities.putTemporaryGlobal(gameData, watcher, tapped.getId(), 0, tapped.getId());
        }
    }

    /**
     * Hands the becomes-tapped ability of {@code read} of {@code source} to the stack door for {@code tapped}, which
     * {@code tappedControllerId} controls: its filter on the tapped permanent ("a Merfolk you control") and "one or
     * more" for the permanents tapped together are judged here, its limit and intervening "if" by the door, and a target
     * is chosen as it goes on the stack (CR 603.3d), a "you may pay" one too (Surgespanner); a "choose one" announces
     * its mode there (CR 603.3c).
     */
    private void putTapAbility(GameData gameData, Permanent source, UUID controllerId, TapRead read,
                               Permanent tapped, UUID tappedControllerId) {
        if (skipsBatchedTapTrigger(gameData, source, read)
                || !tappedPermanentMatches(gameData, source, controllerId, tapped, read)) {
            return;
        }
        if (abilities.put(gameData, read.ability(), source.getCard(), source, controllerId,
                ConditionContext.forPermanent(source, controllerId).withTriggeringPermanentId(tapped.getId()),
                tappedRecord(source, tapped, tappedControllerId, null))) {
            markBatchedTapTrigger(gameData, source, read);
        }
    }

    /**
     * Whether {@code read}'s "one or more" ability already triggered on {@code source} for the permanents tapped
     * together in the open tap batch ({@code TriggerCollectionService.beginPermanentTapTriggerBatch}): the mark is the
     * ability's ({@link TriggeredAbility#limitKey()}), so two "one or more" abilities of one permanent each trigger once.
     */
    private static boolean skipsBatchedTapTrigger(GameData gameData, Permanent source, TapRead read) {
        return read.oneOrMore()
                && gameData.permanentTapTriggerBatchDepth > 0
                && gameData.permanentTapTriggerBatchFiredAbilities.getOrDefault(source.getId(), Set.of())
                .contains(read.ability().limitKey());
    }

    private static void markBatchedTapTrigger(GameData gameData, Permanent source, TapRead read) {
        if (!read.oneOrMore() || gameData.permanentTapTriggerBatchDepth <= 0) {
            return;
        }
        gameData.permanentTapTriggerBatchFiredAbilities
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(read.ability().limitKey());
    }

    /**
     * The emblems' "whenever a [filter] permanent you control becomes tapped" abilities for {@code tappedPermanent},
     * which {@code tappedPermanentControllerId} controls (Tezzeret, Betrayer of Flesh's emblem). Abilities of emblems
     * function in the command zone (CR 114.4). Each ability whose trigger's filter the permanent matches goes on the
     * stack once as one object of its effects (CR 603.3), the tapped permanent as its triggering permanent. An ability
     * with an option or a target fails loudly instead.
     */
    private void emblemTapped(GameData gameData, Permanent tappedPermanent, UUID tappedPermanentControllerId) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesTapped(gameData, emblem, ability, tappedPermanent, tappedPermanentControllerId)) {
                    putEmblemAbility(gameData, emblem, ability, tappedPermanent, tappedPermanentControllerId);
                }
            }
        }
    }

    private boolean emblemSeesTapped(GameData gameData, Emblem emblem, TriggeredAbility ability, Permanent tapped,
                                     UUID tappedControllerId) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof TapTrigger trigger)
                    || trigger.event() != TapTrigger.TapEvent.BECOMES_TAPPED) {
                continue;
            }
            if (!ability.options().equals(TriggerOptions.DEFAULT) || ability.effects().stream()
                    .anyMatch(registration -> registration.effect().targetSpec() != TargetSpec.NONE)) {
                throw new IllegalStateException(EventAbilitySupport.emblemName(emblem) + " has a tap ability its "
                        + "reader doesn't judge: " + ability);
            }
            FilterContext context = FilterContext.of(gameData).withSourceControllerId(emblem.controllerId());
            if (emblem.sourceCard() != null) {
                context = context.withSourceCardId(emblem.sourceCard().getId());
            }
            if (controlledByRelatedPlayer(trigger.relation(), emblem, tappedControllerId)
                    && (trigger.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(tapped, trigger.filter(), context))
                    && (trigger.qualifier() == null
                    || predicateEvaluationService.matchesPermanentPredicate(tapped, trigger.qualifier(), context))) {
                return true;
            }
        }
        return false;
    }

    private static boolean controlledByRelatedPlayer(ObjectRelation relation, Emblem emblem, UUID controllerId) {
        return switch (relation) {
            case YOU_CONTROL, ANOTHER_YOU_CONTROL -> emblem.controllerId().equals(controllerId);
            case OPPONENT_CONTROLS -> !emblem.controllerId().equals(controllerId);
            case ANY, ANOTHER -> true;
            default -> throw new IllegalStateException(EventAbilitySupport.emblemName(emblem) + " has a trigger "
                    + "relation its reader doesn't judge: " + relation);
        };
    }

    /**
     * Puts {@code ability} of {@code emblem} on the stack for {@code triggering} becoming tapped, as its triggering
     * permanent. An emblem without a source card (one a test built) is described as "Emblem's emblem", with the
     * triggering permanent's card as its source.
     */
    private void putEmblemAbility(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                  Permanent triggering, UUID triggeringControllerId) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : triggering.getCard();
        String description = EventAbilitySupport.emblemName(emblem);
        abilities.putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()).withTriggeringPermanentId(triggering.getId()),
                entry -> {
                    entry.setTriggeringPermanentId(triggering.getId());
                    entry.setTriggeringPermanentControllerId(triggeringControllerId);
                    entry.setTriggeringCardId(triggering.getCard().getId());
                    entry.setNonTargeting(true);
                });
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + triggering.getCard().getName()
                + " becoming tapped."));
        log.info("Game {} - {} triggers for {} becoming tapped", gameData.id, description,
                triggering.getCard().getName());
    }

    /** "Whenever this creature becomes tapped to pay a teamwork cost". */
    private void tappedForTeamwork(GameData gameData, Permanent tappedCreature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, tappedCreature.getId());
        if (controllerId == null) {
            return;
        }
        for (TriggeredAbility ability : abilities.abilities(gameData, tappedCreature, TEAMWORK)) {
            requireNothingToJudge(read(ability, TEAMWORK).getFirst(), TEAMWORK);
            abilities.put(gameData, ability, tappedCreature.getCard(), tappedCreature, controllerId,
                    ConditionContext.forPermanent(tappedCreature, controllerId),
                    EventAbilitySupport.eventRecord(tappedCreature, null, controllerId, null, null, null));
        }
    }

    /**
     * {@code untappedPermanent} became untapped: the delayed "when this creature becomes untapped, destroy that
     * creature" actions (Merieke Ri Berit), its own "whenever this permanent becomes untapped" (Hollowsage; those
     * granted to it, Well Rested, whose source is the enchanted creature), its controller's "whenever a permanent you
     * control becomes untapped" (Wake Thrasher), then every "whenever a permanent becomes untapped".
     */
    private void becameUntapped(GameData gameData, Permanent untappedPermanent) {
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(untappedPermanent)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) return;

        triggerCollectionService.processDelayedDestroyTargetWhenSourceUntaps(gameData, untappedPermanent);

        // "This ability triggers only once each turn" (Well Rested) is the ability's limit; each granting Aura's
        // instance keeps its own (TriggeredAbility#instance()).
        for (TriggeredAbility ability : abilities.abilities(gameData, untappedPermanent, SELF_UNTAPPED)) {
            TapRead read = read(ability, SELF_UNTAPPED).getFirst();
            if (read.filter() != null || read.firstTime() || read.oneOrMore()) {
                throw new IllegalStateException("The matcher of " + SELF_UNTAPPED + " judges no filter, first time "
                        + "or \"one or more\": " + ability);
            }
            abilities.put(gameData, ability, untappedPermanent.getCard(), untappedPermanent, controllerId,
                    ConditionContext.forPermanent(untappedPermanent, controllerId),
                    EventAbilitySupport.eventRecord(untappedPermanent, null, controllerId, null, null, null));
        }

        UUID untappedControllerId = controllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(untappedControllerId)) return;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, PERMANENT_YOU_CONTROL_UNTAPPED)) {
                TapRead read = read(ability, PERMANENT_YOU_CONTROL_UNTAPPED).getFirst();
                if (read.limit() != TriggerLimit.NONE) {
                    throw new IllegalStateException("The matcher of " + PERMANENT_YOU_CONTROL_UNTAPPED
                            + " judges no limit: " + ability);
                }
                if (!tappedPermanentMatches(gameData, perm, ownerId, untappedPermanent, read)) continue;
                abilities.put(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId)
                                .withTriggeringPermanentId(untappedPermanent.getId()),
                        tappedRecord(perm, untappedPermanent, untappedControllerId, null));
            }
        });

        gameData.forEachPermanent((ownerId, perm) -> {
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, PERMANENT_UNTAPPED)) {
                TapRead read = read(ability, PERMANENT_UNTAPPED).getFirst();
                if (!tappedPermanentMatches(gameData, perm, ownerId, untappedPermanent, read)) continue;
                TriggerRecord untapped = tappedRecord(perm, untappedPermanent, untappedControllerId, null);
                abilities.put(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId)
                                .withTriggeringPermanentId(untappedPermanent.getId()), entry -> {
                            untapped.stamp(entry);
                            entry.setEventPlayerIds(List.of(untappedControllerId));
                        });
            }
        });
    }

    /**
     * "Whenever you untap one or more permanents during your untap step, …" (The Millennium Calendar's "put that many
     * time counters"), once for the untap step, with the number of permanents untapped as its event value ("that
     * many").
     */
    private void untapStep(GameData gameData, UUID controllerId, int untappedPermanentCount) {
        if (untappedPermanentCount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, permanent, UNTAP_STEP)) {
                requireNothingToJudge(read(ability, UNTAP_STEP).getFirst(), UNTAP_STEP);
                abilities.put(gameData, ability, permanent.getCard(), permanent, controllerId,
                        ConditionContext.forPermanent(permanent, controllerId).withEventValue(untappedPermanentCount),
                        EventAbilitySupport.eventRecord(permanent, null, controllerId, untappedPermanentCount, null,
                                null));
            }
        }
    }

    /**
     * The record of a becomes-tapped or becomes-untapped event, whatever the ability's effects are: the permanent as
     * its triggering permanent ("it", "that permanent's controller", Royal Decree; as it last existed on the
     * battlefield once it has left, CR 608.2h), {@code thatPlayerId} as the non-targeting "that player" (or none), and
     * the source as it was when the ability triggered.
     */
    private static TriggerRecord tappedRecord(Permanent source, Permanent tapped, UUID tappedControllerId,
                                              UUID thatPlayerId) {
        Permanent snapshot = new Permanent(source);
        return entry -> {
            entry.setNonTargeting(true);
            if (thatPlayerId != null) {
                entry.setTargetId(thatPlayerId);
            }
            entry.setTriggeringPermanentId(tapped.getId());
            entry.setTriggeringPermanent(tapped);
            entry.setTriggeringPermanentControllerId(tappedControllerId);
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Whether the tapped or untapped permanent {@code tapped} matches {@code read}'s filter ("a Merfolk you control",
     * "this permanent"), if it has one, judged with {@code source}, the ability's source controlled by
     * {@code sourceControllerId}, as the filter's source.
     */
    private boolean tappedPermanentMatches(GameData gameData, Permanent source, UUID sourceControllerId,
                                           Permanent tapped, TapRead read) {
        if (read.filter() == null) {
            return true;
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(sourceControllerId);
        return predicateEvaluationService.matchesPermanentPredicate(tapped, read.filter(), filterContext);
    }

    /**
     * One effect of a tap ability as its matcher reads it: the effect with the ability's intervening "if" wrapped
     * around it (to be checked again as it resolves, CR 603.4), the ability, the filter on the tapped permanent, "for
     * the first time during your turn" and the tap relation the matcher judges ({@link ObjectRelation#ANY} for none).
     */
    record TapRead(CardEffect effect, TriggeredAbility ability, PermanentPredicate filter, boolean firstTime,
                   ObjectRelation tapRelation) {

        TapRead {
            Objects.requireNonNull(effect, "effect");
            Objects.requireNonNull(ability, "ability");
            Objects.requireNonNull(tapRelation, "tapRelation");
        }

        TriggerLimit limit() {
            return ability.options().limit();
        }

        boolean oneOrMore() {
            return ability.options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        }

        /**
         * The effect as an ability that triggered was judged: a triggered mana ability resolves at once, so its
         * intervening "if", judged as it triggered, isn't wrapped around it again.
         */
        CardEffect effectAsJudged() {
            Condition interveningIf = ability.options().interveningIf();
            if (interveningIf != null && effect instanceof ConditionalEffect conditional
                    && conditional.condition() == interveningIf && isTriggeredManaAbility(ability)) {
                return conditional.wrapped();
            }
            return effect;
        }
    }

    /**
     * The effects of {@code abilities} that {@code judge} lets trigger, one list per ability, each read as judged
     * ({@link TapRead#effectAsJudged}); an ability with "for the first time", "one or more" or a limit throws.
     */
    private static List<List<TapRead>> readAsJudged(List<TriggeredAbility> abilities, Trigger watched,
                                                    TriggerJudge judge) {
        List<List<TapRead>> byAbility = new ArrayList<>();
        for (TriggeredAbility ability : judge.filter(abilities)) {
            List<TapRead> reads = read(ability, watched);
            reads.forEach(read -> {
                if (read.firstTime() || read.oneOrMore() || read.limit() != TriggerLimit.NONE) {
                    throw new IllegalStateException("The matcher of " + watched + " reads no first time, \"one or "
                            + "more\" or limit: " + read.ability());
                }
            });
            if (!reads.isEmpty()) {
                byAbility.add(reads.stream().map(read -> new TapRead(read.effectAsJudged(), read.ability(),
                        read.filter(), read.firstTime(), read.tapRelation())).toList());
            }
        }
        return byAbility;
    }

    /** The effects of each of {@code abilities}, one list per ability, for a matcher that judges nothing of them. */
    private static List<List<CardEffect>> strictEffects(List<TriggeredAbility> abilities, Trigger watched) {
        List<List<CardEffect>> byAbility = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            List<TapRead> reads = read(ability, watched);
            reads.forEach(read -> requireNothingToJudge(read, watched));
            if (!reads.isEmpty()) {
                byAbility.add(effectsOf(reads));
            }
        }
        return byAbility;
    }

    private static List<CardEffect> effectsOf(List<TapRead> reads) {
        return reads.stream().map(TapRead::effect).toList();
    }

    /** The effects of {@code ability}, as its matcher reads them for the condition watched as {@code watched}. */
    static List<TapRead> read(TriggeredAbility ability, Trigger watched) {
        TapTrigger condition = watchedCondition(ability, watched);
        TriggerOptions options = ability.options();
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN) {
            throw new IllegalStateException("No tap or untap matcher reads " + options.limit() + ": " + ability);
        }
        boolean firstTime = condition.event() == TapTrigger.TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN;
        if (firstTime && options.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("No tap matcher reads a limit next to \"for the first time during your "
                    + "turn\": " + ability);
        }
        PermanentPredicate filter = filter(condition);
        Condition interveningIf = options.interveningIf();
        List<TapRead> reads = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            if (interveningIf != null) {
                effect = new ConditionalEffect(interveningIf, effect);
            }
            reads.add(new TapRead(effect, ability, filter, firstTime,
                    condition.judgedTapRelation() ? condition.relation() : ObjectRelation.ANY));
        }
        return reads;
    }

    /** Throws when {@code read}'s ability has a filter, tap relation, first time, "one or more" or limit. */
    private static void requireNothingToJudge(TapRead read, Trigger watched) {
        if (read.filter() != null || read.tapRelation() != ObjectRelation.ANY || read.firstTime() || read.oneOrMore()
                || read.limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The matcher of " + watched + " judges nothing of " + read.ability());
        }
    }

    /**
     * Whether {@code ability} is a triggered mana ability (CR 605.1b): every trigger condition is a tap for mana, and
     * its effects (a sequence's steps) declare no target and could add mana.
     */
    public static boolean isTriggeredManaAbility(TriggeredAbility ability) {
        boolean fromManaAbility = ability.conditions().stream().allMatch(condition
                -> condition.trigger() instanceof TapTrigger tap
                && tap.event() == TapTrigger.TapEvent.TAPPED_FOR_MANA);
        List<CardEffect> effects = ability.effects().stream()
                .flatMap(registration -> steps(registration.effect()).stream())
                .toList();
        return fromManaAbility && isTriggeredManaEffect(effects);
    }

    private static boolean isTriggeredManaEffect(List<CardEffect> effects) {
        return effects.stream().noneMatch(effect -> effect.targetSpec().declaredTarget() != null)
                && effects.stream().anyMatch(TriggeredManaAbilityEffect.class::isInstance);
    }

    private static boolean isTriggeredManaAbility(List<CardEffect> effects) {
        return isTriggeredManaEffect(steps(effects));
    }

    private static List<CardEffect> steps(List<CardEffect> effects) {
        return effects.stream().flatMap(effect -> steps(effect).stream()).toList();
    }

    private static List<CardEffect> steps(CardEffect effect) {
        return effect instanceof SequenceEffect sequence ? sequence.steps() : List.of(effect);
    }

    /**
     * {@code ability}'s tap condition watched as {@code watched}. Its conditions watched so must agree on the filter,
     * event and tap relation.
     */
    private static TapTrigger watchedCondition(TriggeredAbility ability, Trigger watched) {
        List<TapTrigger> conditions = ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(TapTrigger.class::isInstance).map(TapTrigger.class::cast)
                .filter(tap -> tap.watchKey().equals(watched)).distinct().toList();
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException(ability + " has no tap condition watched as " + watched);
        }
        TapTrigger first = conditions.getFirst();
        if (conditions.stream().anyMatch(tap -> tap.event() != first.event()
                || tap.judgedTapRelation() != first.judgedTapRelation()
                || (tap.judgedTapRelation() && tap.relation() != first.relation())
                || !Objects.equals(filter(tap), filter(first)))) {
            throw new IllegalStateException("The tap conditions of one ability watched as " + watched
                    + " must have the same filter, event and tapping player: " + ability);
        }
        return first;
    }

    /**
     * The filter on the tapped or untapped permanent a tap condition's matcher judges: "this permanent" and the
     * narrowed filter of a narrowed condition, and the qualifier.
     */
    private static PermanentPredicate filter(TapTrigger trigger) {
        if (trigger.qualifier() != null && !judgesFilter(trigger)) {
            throw new IllegalStateException("No tap or untap matcher judges a filter on " + trigger);
        }
        List<PermanentPredicate> parts = new ArrayList<>();
        if (trigger.narrowed() && trigger.relation() == ObjectRelation.THIS) {
            parts.add(new PermanentIsSourceCardPredicate());
        }
        if (trigger.narrowed() && trigger.filter() != null) {
            parts.add(trigger.filter());
        }
        if (trigger.qualifier() != null) {
            parts.add(trigger.qualifier());
        }
        return switch (parts.size()) {
            case 0 -> null;
            case 1 -> parts.getFirst();
            default -> new PermanentAllOfPredicate(List.copyOf(parts));
        };
    }

    private static boolean judgesFilter(TapTrigger trigger) {
        if (trigger.narrowed()) {
            return true;
        }
        return switch (trigger.event()) {
            case BECOMES_TAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case YOU_TAP -> trigger.relation() == ObjectRelation.OPPONENT_CONTROLS;
            case TAPPED_FOR_MANA -> trigger.watchKey().equals(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA,
                    ObjectRelation.ANY, Triggers.LAND))
                    || trigger.watchKey().equals(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA,
                    ObjectRelation.ANY));
            case BECOMES_UNTAPPED -> trigger.relation() == ObjectRelation.YOU_CONTROL
                    || trigger.relation() == ObjectRelation.ANY;
            default -> false;
        };
    }
}
