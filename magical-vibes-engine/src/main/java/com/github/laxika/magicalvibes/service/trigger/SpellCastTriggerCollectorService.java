package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.condition.SpellManaValueEqualsSourceCounters;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryCouldTargetAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

import com.github.laxika.magicalvibes.service.target.ValidTargetService;

/**
 * The generic spell-cast, spell-copy and card-advantage collectors: a cast ability, filtered or "any spell", is judged
 * and put on the stack recording the spell, its caster and the cast event's value whatever its effects are.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TargetLegalityService targetLegalityService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ValidTargetService validTargetService;

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

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
    public boolean collectCastAbility(TriggerMatchContext match, CastAbility ability, TriggerContext.SpellCast sc,
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
    public boolean collectCardAdvantageAbility(TriggerMatchContext match, TriggeredAbility ability,
                                               TriggerContext.SpellCardAdvantage event) {
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
    public boolean collectCopyAbility(TriggerMatchContext match, TriggeredAbility ability, SpellFilter filter,
                                      TriggerContext.SpellCopy spellCopy) {
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
                    .anyMatch(SpellCastTriggerCollectorService::mentionsXInManaCost);
            case CardAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SpellCastTriggerCollectorService::mentionsXInManaCost);
            case CardNotPredicate not -> mentionsXInManaCost(not.predicate());
            default -> false;
        };
    }

    private boolean handleGenericSpellCastTrigger(TriggerMatchContext match, CastAbility trigger,
                                                    TriggerContext.SpellCast spellCast) {
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

    static Card spellOnStackWithChosenX(Card spellCard, Integer chosenX) {
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
                    .anyMatch(SpellCastTriggerCollectorService::readsManaValueX);
            case PermanentAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SpellCastTriggerCollectorService::readsManaValueX);
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

}
