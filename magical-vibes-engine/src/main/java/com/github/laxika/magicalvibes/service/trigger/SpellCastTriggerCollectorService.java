package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
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
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import com.github.laxika.magicalvibes.model.GameLog;
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

    /**
     * Collects a "whenever [a player] casts a [quality] spell" ability: judges its spell filter and intervening "if"
     * and puts it on the stack, its targets chosen as it does (CR 603.3d). {@code perManaFromSource} says whether a
     * "when you spend this mana to cast …" rider is collected once for each mana its source produced that was spent
     * on the spell: such a rider on a mana ability is a separate delayed trigger for each of them (CR 106.6), which
     * the "whenever you cast" collector reads.
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
     * advantage: the ability goes on the stack as written, one object of its effects (CR 603.3), with the spell
     * recorded as the entry's triggering spell. Its reader judges its limit; an option, a target or a "you may" the
     * reader doesn't read throws.
     *
     * @return whether the ability triggered
     */
    public boolean collectCardAdvantageAbility(TriggerMatchContext match, TriggeredAbility ability,
                                               TriggerContext.SpellCardAdvantage event) {
        TriggerOptions options = ability.options();
        if (options.interveningIf() != null || options.eventCondition() != null
                || options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The card-advantage reader judges no intervening \"if\", event condition "
                    + "or multiplicity: " + ability);
        }
        List<CardEffect> effects = ability.effects().stream().map(EffectRegistration::effect).toList();
        if (effects.stream().anyMatch(effect -> triggerTargetSpec(effect).declaredTarget() != null)) {
            throw new IllegalStateException("The card-advantage reader chooses no target: " + ability);
        }
        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : match.permanent().getCard();
        StackEntry spell = event.spell();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                abilityDescription(sourceCard), new ArrayList<>(effects), null,
                match.permanent() == null ? null : match.permanent().getId());
        entry.setTriggeringCardId(spell.getTargetableId());
        entry.setTriggeringSpell(spell);
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
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
     * against the copy trigger's {@code filter} (a copy isn't cast, so that is all the filter says) and puts the
     * ability's {@code effects} on the stack as one object (CR 603.3), its targets chosen as it does (CR 603.3d) from
     * the target group its effects are bound to.
     *
     * @return whether the ability triggered
     */
    public boolean collectCopyAbility(TriggerMatchContext match, SpellFilter filter, List<CardEffect> effects,
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

        List<CardEffect> resolved = List.copyOf(effects);
        // A copy ability declares its target as a target group of the card, bound to its effects.
        Card sourceCard = match.permanent().getCard();
        List<Integer> boundGroups = boundTargetGroups(sourceCard, effects);
        TargetFilter targetFilter = boundTargetFilter(sourceCard, effects);
        boolean optionalTarget = !boundGroups.isEmpty()
                && sourceCard.getTargetGroups().get(boundGroups.getFirst()).getMinTargets() == 0;
        Card copiedCard = spellCopy.copiedSpell().getCard();
        int copiedSpellManaValue = copiedCard.getManaValue()
                + (copiedCard.getParsedManaCost() == null ? 0
                : spellCopy.copiedSpell().getXValue() * copiedCard.getParsedManaCost().getXSymbolCount());
        boolean needsPlayerTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PERMANENT));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                resolved,
                spellCopy.copyingPlayerId(),
                match.permanent().getId());
        entry.setTriggeringCardId(spellCopy.copiedSpell().getCard().getId());
        entry.setTriggeringSpell(spellCopy.copiedSpell());
        entry.setEventValue(copiedSpellManaValue);
        entry.setNonTargeting(true);
        match.gameData().handOffTriggerRecord(entry);
        if (needsPlayerTarget || needsPermanentTarget) {
            // "That spell": the copy the event was about, which the entry takes as its triggering spell once the
            // targets are chosen.
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), resolved,
                    needsPlayerTarget && !needsPermanentTarget, targetFilter, 0,
                    match.permanent().getId(), null, optionalTarget, null, null,
                    match.controllerId(), copiedSpellManaValue, null, false,
                    spellCopy.copiedSpell().getCard().getId()));
            gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
            return true;
        }

        boolean needsGraveyardTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsGraveyardTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), resolved));
            return true;
        }

        match.gameData().stack.add(entry);
        return true;
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
            TriggerMatchContext spellSourced = new TriggerMatchContext(match.gameData(), match.permanent(),
                    match.controllerId(), match.effects(), spellCard, match.sourcePlanarObject(),
                    match.onceEachTurnActionKey(), match.triggersOnlyOnceEachTurn());
            for (CardEffect cascade : trigger.effects()) {
                queued |= handleGenericSpellCastTrigger(spellSourced, trigger.withEffects(List.of(cascade)),
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
        // (CR 715.3b), so its colors, types and mana cost are read from that half.
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
        // An intervening "if" on the triggering spell ("if it has the same name as one of the cards exiled with this
        // artifact", Spellweaver Helix) is judged on the spell as it is cast, from the ability's source, and again as
        // the ability resolves through the ConditionalEffect around each effect (CR 603.4).
        if (trigger.interveningIf() instanceof TriggeringSpellMatches matches) {
            if (triggeringSpell == null || !matchesCastSpellPredicate(match, triggeringSpell, matches.filter(),
                    exiledSourcePermanentId)) {
                return false;
            }
        } else if (trigger.interveningIf() != null
                && !conditionEvaluationService.isMet(match.gameData(), trigger.interveningIf(),
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

        // A lone plain "you may [pay {N}]" goes on the stack as the card wrote it and is asked as the ability resolves
        // (CR 603.5); the targets of what it offers are chosen now (CR 603.3d), so the effects are judged without it.
        List<CardEffect> resolved = new ArrayList<>(trigger.effectsTargetsAreChosenFor());
        List<CardEffect> optionalAbility = resolved.equals(trigger.effects()) ? null
                : trigger.effects().stream()
                        .map(effect -> trigger.interveningIf() == null ? effect
                                : (CardEffect) new ConditionalEffect(trigger.interveningIf(), effect))
                        .toList();
        if (trigger.interveningIf() != null) {
            resolved = resolved.stream()
                    .map(effect -> (CardEffect) new ConditionalEffect(trigger.interveningIf(), effect))
                    .toList();
        }
        boolean needsPlayerTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PERMANENT));
        boolean needsGraveyardTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean needsSpellTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.SPELL));
        boolean needsTargeting = needsPlayerTarget || needsPermanentTarget;
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
        // A permanent target restricted by "that spell's mana value" (Skyfire Kirin) is chosen now, as the ability goes
        // on the stack (CR 603.3d), against the spell's mana value as the ability's X.
        boolean targetRestrictedBySpellManaValue = needsPermanentTarget && readsManaValueX(
                trigger.targetFilter() != null ? trigger.targetFilter() : boundTargetFilter(sourceCard, trigger.effects()));
        // Otherwise the ability's X is the spell's: an effect's own filter "with that spell's mana value" (Celestial
        // Kirin's destroy, Infernal Kirin's discard), "the amount of mana spent to cast that spell" (ManaSpentToCast, the
        // mana-spent conditions) and "for each color of mana spent to cast that spell" (ColorsSpentToCast) read the
        // triggering spell as the ability resolves.
        int abilityX = targetRestrictedBySpellManaValue
                ? spellManaValue(match.gameData(), spellCard)
                : spellXValue != null ? spellXValue : 0;
        // The cast event's value, recorded on every entry and choice whatever the effects are: the number of kicks for a
        // kicked-spell ability, else the spell's mana value as the ability triggers (with the chosen X, CR 202.3e). An
        // effect reads "that spell's mana value" as it resolves (TargetSpellManaValue: the live spell, else the entry's
        // snapshot of it, CR 608.2h), so this value is only the fallback where neither is kept.
        int triggeringSpellManaValue = (trigger.filter().kicker() != SpellFilter.Kicker.ANY) ? kickCount
                : spellManaValue(match.gameData(), spellCard);

        if (optionalAbility != null) {
            resolved = optionalAbility;
        }
        // The entry of an ability that chooses nothing, built first: a mode, a target or a graveyard card chosen as the
        // ability goes on the stack is handed it as the record of the cast.
        StackEntry entry = abilityX > 0
                ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                    abilityDescription(sourceCard), resolved, abilityX,
                    null, sourcePermanentId, null, null, null, null)
                : new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                    abilityDescription(sourceCard), resolved, null,
                    sourcePermanentId);
        entry.setTargetId(castingPlayerId);
        entry.setNonTargeting(true);
        // The player the cast was about, whom "another target player" (The Lord of Pain) excludes.
        entry.setTriggeringPlayerId(castingPlayerId);
        // Contextual "it" = the spell that caused the trigger, still on the stack below this
        // ability (Bloodlord of Vaasgoth's bloodthirst grant), and as it was then once it has left (CR 608.2h).
        entry.setTriggeringCardId(spellCard.getId());
        entry.setTriggeringSpell(triggeringSpell);
        entry.setTriggeringSpellCastIndex(
                match.gameData().getSpellCastIndexThisTurn(castingPlayerId, spellCard.getId()));
        entry.setTriggeringSpellTurnCastIndex(match.gameData().getSpellCastOrdinalThisTurn(spellCard.getId()) - 1);
        // "Its targets" (Bloodlord of Vaasgoth's grant) are the spell's targets as it was cast, kept once it has left.
        if (triggeringSpell != null) {
            List<UUID> spellTargets = new ArrayList<>();
            if (triggeringSpell.getTargetId() != null) {
                spellTargets.add(triggeringSpell.getTargetId());
            }
            spellTargets.addAll(triggeringSpell.getDeclaredTargetIds());
            entry.setTriggeringSpellTargetIds(List.copyOf(spellTargets));
        }
        entry.setTriggeringCardSnapshot(spellCard);
        entry.setEventValue(triggeringSpellManaValue);
        if (interveningSpellManaValueCondition) {
            entry.setEventValue(interveningSpellManaValue);
        }
        // The source as the ability goes on the stack, whatever the effects are: its last known information once it
        // has left the battlefield (CR 608.2h), and what a transform instruction compares (CR 701.27f).
        if (match.permanent() != null) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        preservePlanarSource(entry, match);
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        match.gameData().handOffTriggerRecord(entry);
        if (resolved.size() == 1 && resolved.getFirst() instanceof ChooseOneAtTriggerTimeEffect atTriggerTime
                && atTriggerTime.maximumChoices() != null) {
            // "Choose up to X" (Riku of Many Paths): X is read as the ability goes on the stack, where its modes are
            // chosen (CR 603.3c), from the triggering spell; with no mode to choose, the ability is removed (CR 700.2b).
            StackEntry ability = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                    abilityDescription(sourceCard), resolved);
            ability.setTriggeringCardId(spellCard.getId());
            ability.setTriggeringSpell(triggeringSpell);
            ChooseOneEffect choice = atTriggerTime.choice();
            int maximumChoices = Math.min(choice.choicesMax(), Math.max(0, amountEvaluationService.evaluate(
                    match.gameData(), atTriggerTime.maximumChoices(),
                    AmountContext.forStackEntry(ability, match.permanent()))));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            if (maximumChoices == 0) {
                gameLogService.append(match.gameData(), GameLog.cardThen(sourceCard,
                        "'s triggered ability has no modes to choose."));
                match.gameData().handOffTriggerRecord(null);
                return true;
            }
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    sourceCard, match.controllerId(), choice, sourcePermanentId, spellCard.getId())
                    .withModeCount(0, maximumChoices));
        } else if (resolved.size() == 1 && resolved.getFirst() instanceof ChooseOneEffect chooseOneEffect) {
            // The mode is chosen as the ability goes on the stack (CR 603.3c); a "hasn't been chosen (this turn)"
            // modal offers only the modes its source hasn't chosen (TriggeredModalTrigger reads the restriction).
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    sourceCard, match.controllerId(), chooseOneEffect, sourcePermanentId,
                    spellCard.getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        } else if (needsGraveyardTarget) {
            // The graveyard target is chosen now, as the ability goes on the stack (CR 603.3d), with the X the entry
            // of an ability that chooses nothing has: a target restricted by "that spell's mana value" (Krile
            // Baldesion) reads the triggering spell from the record, as the entry does as it resolves.
            // A graveyard target restricted by "that spell" ("the same name as that spell", Harness the Storm) is chosen
            // against the triggering spell, which the stack entry keeps for the check as the ability resolves.
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, match.controllerId(), resolved, null, 0, abilityX, 0, null, false, null,
                    sourcePermanentId, match.onceEachTurnActionKey()).withTriggeringSpell(triggeringSpell));
            log.info("Game {} - {} spell-cast graveyard-target trigger queued",
                    match.gameData().id, sourceCard.getName());
        } else if (needsSpellTarget && !needsTargeting) {
            StackEntry spellTargetEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    abilityDescription(sourceCard),
                    resolved,
                    0,
                    spellCard.getId(),
                    sourcePermanentId,
                    Map.of(),
                    Zone.STACK,
                    List.of(),
                    List.of()
            );
            spellTargetEntry.setTriggeringCardId(spellCard.getId());
            spellTargetEntry.setTriggeringSpell(triggeringSpell);
            spellTargetEntry.setTriggeringSpellCastIndex(
                    match.gameData().getSpellCastIndexThisTurn(castingPlayerId, spellCard.getId()));
            spellTargetEntry.setTriggeringSpellTurnCastIndex(
                    match.gameData().getSpellCastOrdinalThisTurn(spellCard.getId()) - 1);
            spellTargetEntry.setEventValue(triggeringSpellManaValue);
            // The caster stays known if the spell leaves the stack (e.g. is countered) before this resolves.
            spellTargetEntry.setCounteredSpellControllerId(castingPlayerId);
            preservePlanarSource(spellTargetEntry, match);
            spellTargetEntry.setNonTargeting(true);
            // This entry chooses nothing either and is its own record.
            match.gameData().handOffTriggerRecord(spellTargetEntry);
            match.gameData().stack.add(spellTargetEntry);
        } else if (needsTargeting) {
            // "You may pay {C}. If you do, [targeted effect]" (Malachite Talisman): the target is chosen
            // as the trigger goes on the stack (CR 603.3d), while the payment choice waits for resolution
            // (CR 603.5).
            List<CardEffect> queued = resolved;
            // The ability's own target groups when its effects are bound to them, else every group of the card.
            List<Integer> boundGroups = boundTargetGroups(sourceCard, trigger.effects());
            List<com.github.laxika.magicalvibes.model.TargetGroup> abilityGroups = boundGroups.isEmpty()
                    ? sourceCard.getTargetGroups()
                    : boundGroups.stream().map(sourceCard.getTargetGroups()::get).toList();
            boolean multiTarget = abilityGroups.size() > 1
                    || abilityGroups.stream()
                    .anyMatch(target -> target.getMaxTargets() > 1 || target.getMinTargets() == 0);
            if (multiTarget) {
                // The walk chooses against the X the entry of an ability that chooses nothing has, as the
                // single-target choice below does.
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        sourceCard, match.controllerId(), queued, sourcePermanentId,
                        List.of(), 0, 0, List.of(), abilityX,
                        List.of(), false, spellCard.getId(),
                        null, triggeringSpellManaValue));
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        sourceCard, match.controllerId(), queued, playerTargetOnly,
                        trigger.targetFilter() != null ? trigger.targetFilter()
                                : boundTargetFilter(sourceCard, trigger.effects()),
                        abilityX, sourcePermanentId, null, false, null, null,
                        match.controllerId(), triggeringSpellManaValue,
                        match.sourcePlanarObject() == null ? null : match.sourcePlanarObject().copy(),
                        false, spellCard.getId()
                ).withTriggeringPlayer(castingPlayerId));
            }
            gameLogService.append(match.gameData(), GameLog.cardThen(sourceCard,
                    "'s triggered ability triggers — choose a target."));
        } else {
            // An ability that chooses no target goes on the stack the same way whatever its effects are. "That
            // player" is the caster, the entry's non-targeting player; "that spell" is its triggering card and
            // triggering spell, which "counter that spell", "reveal ... if it shares a card type with that spell" and
            // "any player may pay ... If a player does, counter that spell" read as they resolve: the ability has no
            // target, so it still resolves once the spell has left the stack (CR 608.2b removes only an ability whose
            // targets are all illegal). "Enchanted creature" of an Aura's ability is read from the source, or from its
            // snapshot below once it has left the battlefield (CR 608.2h).
            match.gameData().stack.add(entry);
            // "Whenever a player kicks a spell": once for each time it was kicked.
            for (int kick = 1; (trigger.filter().kicker() == SpellFilter.Kicker.EACH_KICK) && kick < kickCount; kick++) {
                StackEntry again = new StackEntry(entry);
                match.gameData().stack.add(again);
            }
        }
        return true;
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

    private void preservePlanarSource(StackEntry entry, TriggerMatchContext match) {
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
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

    private boolean hasOptionalSingleTarget(Card card, CardEffect effect) {
        if (card.getTargetGroups().size() != 1) {
            return false;
        }
        var target = card.getTargetGroups().getFirst();
        return target.getMinTargets() == 0
                && target.getMaxTargets() == 1
                && card.getEffectTargetIndex(effect) == target.getIndex();
    }

    /**
     * The targeting a spell-cast trigger must resolve before it goes on the stack. A lone plain "you may pay {X}. If
     * you do, [targeted effect]" is judged by what it offers ({@link CastAbility#effectsTargetsAreChosenFor()}); any
     * other {@link MayPayManaEffect} (another player's payment, a dynamic cost) settles everything at resolution, so it
     * opens no target choice.
     */
    private static TargetSpec triggerTargetSpec(CardEffect effect) {
        return effect instanceof MayPayManaEffect ? TargetSpec.NONE : effect.targetSpec();
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
