package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentSelfTargetingEffect;
import com.github.laxika.magicalvibes.model.effect.DuelistsConvocationInternationalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateFoodWhenPlayingCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.CopiedSpellReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentSnapshotRequiredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNameFoundElsewhereEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellChosenColorCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ModalSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
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
 * Trigger collectors for spell-cast events (ON_ANY_PLAYER_CASTS_SPELL,
 * ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
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

    // ── ON_ANY_PLAYER_CASTS_SPELL ──────────────────────────────────────

    @CollectsSpellAbilityTrigger(value = DuelistsConvocationInternationalTriggerEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    @CollectsSpellAbilityTrigger(value = DuelistsConvocationInternationalTriggerEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND)
    private boolean handleDuelistsConvocationInternational(TriggerMatchContext match,
            DuelistsConvocationInternationalTriggerEffect trigger, TriggerContext ctx) {
        Permanent source = match.permanent();
        if (source == null || source.getChosenNumberDigits().size() != 10) {
            return false;
        }
        Card eventCard = ctx instanceof TriggerContext.SpellCast spellCast
                ? spellCast.spellCard() : ((TriggerContext.LandPlayed) ctx).landCard();
        StackEntry spell = ctx instanceof TriggerContext.SpellCast
                ? gameQueryService.findStackEntryByCardId(match.gameData(), eventCard.getId()) : null;
        int manaValue = spell != null && spell.isCastFaceDown() ? 0 : eventCard.getManaValue()
                + (spell == null || eventCard.getParsedManaCost() == null ? 0
                : spell.getXValue() * eventCard.getParsedManaCost().getXSymbolCount());
        boolean matchesDigit = java.util.stream.IntStream.range(0, source.getChosenNumberDigits().size())
                .anyMatch(index -> !source.getCrossedNumberDigitPositions().contains(index)
                        && source.getChosenNumberDigits().get(index) == manaValue);
        if (!matchesDigit) {
            return false;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(),
                match.controllerId(), source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)), null, source.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setTriggeringCardId(eventCard.getId());
        entry.setEventValue(manaValue);
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = ReturnPermanentControlledByPlayerToHandEffect.class, watch = SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleReturnLandOnSpellCast(TriggerMatchContext match,
            ReturnPermanentControlledByPlayerToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player returns a land they control" — carry the casting player on targetId so the
        // resolution handler prompts them (not the enchantment's controller).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Mana Breach doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = DiscardEffect.class, watch = SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleDiscardOnSpellCast(TriggerMatchContext match,
            DiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player discards a card" — carry the casting player on targetId so the
        // TARGET_PLAYER discard lands on them (not the enchantment's controller). Oppression.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Oppression doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = CounterSpellIfNameFoundElsewhereEffect.class, watch = SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCounterIfNameFoundElsewhere(TriggerMatchContext match,
            CounterSpellIfNameFoundElsewhereEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // Bazaar of Wonders: the name check is part of the effect, so it happens on resolution.
        // The cast spell is stamped as the trigger's target (auto-chosen, not player-selected).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.spellCard().getId(),
                Zone.STACK));
        return true;
    }

    // ── ON_CONTROLLER_CASTS_SPELL ──────────────────────────────────────

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

    @CollectsSpellAbilityTrigger(value = BoostEquippedCreatureUntilEndOfTurnEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleBoostEquippedOnSpellCast(TriggerMatchContext match,
            BoostEquippedCreatureUntilEndOfTurnEffect trigger, TriggerContext ctx) {
        // "Whenever you cast a spell, equipped creature gets +X/+Y until end of turn" (Leering Emblem).
        // Carry the source permanent id so the handler can find the equipment and its equipped creature
        // (the effect fizzles at resolution if the Equipment is no longer attached).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} spell-cast equipped-boost trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
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



    @CollectsSpellAbilityTrigger(value = CreateFoodWhenPlayingCardFromTopOfLibraryEffect.class,
            watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleFoodWhenCastingFromLibraryTop(TriggerMatchContext match,
            CreateFoodWhenPlayingCardFromTopOfLibraryEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.castZone() != Zone.LIBRARY
                || !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.filter(),
                match.permanent().getOriginalCard().getId(), match.gameData(), match.controllerId())
                || !match.gameData().oncePerTurnLibraryPlayPermissionsUsedThisTurn
                .contains(match.permanent().getId())) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(CreateTokenEffect.ofFoodToken(1))),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(sc.spellCard().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }

    // ── ON_OPPONENT_CASTS_SPELL ────────────────────────────────────────

    @CollectsSpellAbilityTrigger(value = PutCountersOnSourceEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastPutCountersOnSource(TriggerMatchContext match,
            PutCountersOnSourceEffect trigger, TriggerContext ctx) {
        // "Whenever an opponent casts a spell, put a counter on this creature" (Ammit Eternal, -1/-1).
        // Mandatory — carry the source permanent id so the counter effect knows which permanent to
        // modify.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} opponent-spell-cast put-counters trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsSpellAbilityTrigger(value = LoseLifeUnlessDiscardEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessDiscard(TriggerMatchContext match,
            LoseLifeUnlessDiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = DealDamageToPlayersEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageToCastingOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = DrawCardForTargetPlayerEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentDraws(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = MillEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentMills(TriggerMatchContext match,
            MillEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "That player mills N cards" — carry the casting opponent on targetId so the
        // TARGET_PLAYER mill lands on them (not a chosen target). Memory Erosion. The player is not
        // targeted, so shroud and similar protection do not stop the mill.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = CounterUnlessPaysEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCounterUnlessPays(TriggerMatchContext match,
            CounterUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.spellCard().getId(),
                Zone.STACK
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleRevealTopCardCreatureToBattlefield(TriggerMatchContext match,
            RevealTopCardCreatureToBattlefieldOrMayBottomEffect trigger, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        ));
        return true;
    }

    @CollectsSpellAbilityTrigger(value = LoseLifeUnlessPaysEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessPays(TriggerMatchContext match,
            LoseLifeUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = DamageUnlessPaysEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageUnlessPays(TriggerMatchContext match,
            DamageUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "that player" = the casting opponent (target of the damage / decision maker). Carry the source
        // permanent so the damage resolves with the correct source (prevention keys off it).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsSpellAbilityTrigger(value = DrawCardUnlessPaysEffect.class, watch = SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDrawCardUnlessPays(TriggerMatchContext match,
            DrawCardUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // Casting opponent is stamped on targetId (pay decision); controller draws if they don't pay.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    // ── Shared helpers ─────────────────────────────────────────────────

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

        StackEntry copiedSpellSnapshot = new StackEntry(spellCopy.copiedSpell());
        List<CardEffect> resolved = effects.stream()
                .map(effect -> effect instanceof CopiedSpellReferencingEffect referencing
                        ? referencing.snapshotCopiedSpell(copiedSpellSnapshot)
                        : effect instanceof MayEffect may && may.wrapped() instanceof CopiedSpellReferencingEffect referencing
                        ? new MayEffect(referencing.snapshotCopiedSpell(copiedSpellSnapshot), may.prompt(),
                        may.elseEffect(), may.choicePlayer(), may.choicePlayerId())
                        : effect)
                .toList();
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
        if (targetFilter == null
                && resolved.size() == 1
                && resolved.getFirst() instanceof LoseLifeEffect loseLife
                && loseLife.recipient() == LoseLifeRecipient.TARGET_PLAYER) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    resolved,
                    spellCopy.copyingPlayerId(),
                    match.permanent().getId());
            entry.setTriggeringCardId(spellCopy.copiedSpell().getCard().getId());
            entry.setEventValue(copiedSpellManaValue);
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
            return true;
        }
        boolean needsPlayerTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream()
                .anyMatch(effect -> triggerTargetSpec(effect).admits(TargetPredicate.Kind.PERMANENT));
        if (needsPlayerTarget || needsPermanentTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), resolved,
                    needsPlayerTarget && !needsPermanentTarget, targetFilter, 0,
                    match.permanent().getId(), null, optionalTarget, null, null,
                    match.controllerId(), copiedSpellManaValue, null));
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

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                resolved,
                spellCopy.copyingPlayerId(),
                match.permanent().getId());
        entry.setTriggeringCardId(spellCopy.copiedSpell().getCard().getId());
        entry.setEventValue(copiedSpellManaValue);
        entry.setNonTargeting(true);
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
        if (trigger.effects().size() > 1
                && trigger.effects().stream().allMatch(CascadeEffect.class::isInstance)) {
            boolean queued = false;
            TriggerMatchContext spellSourced = new TriggerMatchContext(match.gameData(), match.permanent(),
                    match.controllerId(), match.rawEffect(), spellCard, match.sourcePlanarObject(),
                    match.markSourceOncePerTurnOnAcceptance(), match.triggersOnlyOnceEachTurn());
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
        if (trigger.effects().stream().anyMatch(
                GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect.class::isInstance)
                && spellCard.getColors().stream().map(CardColor::name).map(ManaColor::valueOf)
                .noneMatch(color -> match.gameData()
                        .getSpellCastSnowManaSpentByColor(spellCard.getId(), color) > 0)) {
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
        boolean selfTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).selfTargeting());
        boolean attachedSelfTarget = resolved.stream().anyMatch(
                e -> e instanceof AttachedPermanentSelfTargetingEffect && e.targetSpec().selfTargeting());
        boolean needsPlayerTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PLAYER));
        boolean needsPermanentTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.PERMANENT));
        boolean needsGraveyardTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean needsSpellTarget = resolved.stream().anyMatch(e -> triggerTargetSpec(e).admits(TargetPredicate.Kind.SPELL));
        boolean needsTargeting = needsPlayerTarget || needsPermanentTarget;
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
        boolean countersTriggeringSpell = resolved.stream().anyMatch(CounterSpellingEffect.class::isInstance);
        // A counter that acts on "that spell" without targeting it still resolves after the spell was countered in
        // response (CR 608.2b): revealing, offering the free cast, flipping for its caster.
        boolean countersWithoutTargetingTheSpell = resolved.stream().anyMatch(effect ->
                effect instanceof CounterSpellingEffect counter && !counter.targetsTriggeringSpell());
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
        if (resolved.size() == 1 && resolved.getFirst() instanceof ChooseOneEffect chooseOneEffect) {
            // The mode is chosen as the ability goes on the stack (CR 603.3c); a "hasn't been chosen (this turn)"
            // modal offers only the modes its source hasn't chosen (TriggeredModalTrigger reads the restriction).
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    sourceCard, match.controllerId(), chooseOneEffect, sourcePermanentId,
                    spellCard.getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        } else if (needsGraveyardTarget) {
            // A graveyard target restricted by "that spell's mana value" (Krile Baldesion) is chosen now, as the ability
            // goes on the stack (CR 603.3d), against the spell's mana value as the choice's X.
            int graveyardTargetX = resolved.stream()
                    .anyMatch(SpellCastTriggerCollectorService::graveyardTargetReadsManaValueX)
                    ? triggeringSpellManaValue : 0;
            // A graveyard target restricted by "that spell" ("the same name as that spell", Harness the Storm) is chosen
            // against the triggering spell, which the stack entry keeps for the check as the ability resolves.
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, match.controllerId(), resolved, null, 0, graveyardTargetX, 0, null, false, null,
                    sourcePermanentId, match.markSourceOncePerTurnOnAcceptance()).withTriggeringSpell(triggeringSpell));
            log.info("Game {} - {} spell-cast graveyard-target trigger queued",
                    match.gameData().id, sourceCard.getName());
        } else if (needsSpellTarget && !needsTargeting) {
            StackEntry entry = new StackEntry(
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
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(triggeringSpell);
            entry.setTriggeringSpellCastIndex(
                    match.gameData().getSpellCastIndexThisTurn(castingPlayerId, spellCard.getId()));
            entry.setTriggeringSpellTurnCastIndex(match.gameData().getSpellCastOrdinalThisTurn(spellCard.getId()) - 1);
            entry.setEventValue(triggeringSpellManaValue);
            // The caster stays known if the spell leaves the stack (e.g. is countered) before this resolves.
            entry.setCounteredSpellControllerId(castingPlayerId);
            preservePlanarSource(entry, match);
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
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
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        sourceCard, match.controllerId(), queued, sourcePermanentId,
                        List.of(), 0, 0, List.of(), targetRestrictedBySpellManaValue ? abilityX : 0,
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
            StackEntry entry;
            if (countersTriggeringSpell) {
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        match.controllerId(),
                        abilityDescription(sourceCard),
                        resolved,
                        abilityX,
                        spellCard.getId(),
                        sourcePermanentId,
                        null,
                        Zone.STACK,
                        null,
                        null
                );
                if (countersWithoutTargetingTheSpell) {
                    entry.setNonTargeting(true);
                }
            } else if (selfTarget) {
                UUID selfTargetId = attachedSelfTarget
                        ? match.permanent().getAttachedTo()
                        : castingPlayerId;
                entry = abilityX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                            abilityDescription(sourceCard), resolved, abilityX,
                            selfTargetId, sourcePermanentId, null, null, null, null)
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                            abilityDescription(sourceCard), resolved, selfTargetId,
                            sourcePermanentId);
                entry.setNonTargeting(true);
            } else {
                entry = abilityX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                            abilityDescription(sourceCard), resolved, abilityX,
                            null, sourcePermanentId, null, null, null, null)
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, match.controllerId(),
                            abilityDescription(sourceCard), resolved, null,
                            sourcePermanentId);
                // Contextual "that player" = the caster (Leshrac's Sigil may-pay look-at-hand).
                // Non-targeting: oracle does not use the word "target".
                entry.setTargetId(castingPlayerId);
                entry.setNonTargeting(true);
            }
            // Contextual "it" = the spell that caused the trigger, still on the stack below this
            // ability (Bloodlord of Vaasgoth's bloodthirst grant), and as it was then once it has left (CR 608.2h).
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(triggeringSpell);
            entry.setTriggeringSpellCastIndex(
                    match.gameData().getSpellCastIndexThisTurn(castingPlayerId, spellCard.getId()));
            entry.setTriggeringSpellTurnCastIndex(match.gameData().getSpellCastOrdinalThisTurn(spellCard.getId()) - 1);
            if (triggeringSpell != null && resolved.stream().anyMatch(effect ->
                    effect instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.TARGETS)) {
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
            if (trigger.interveningIf() != null) {
                if (match.permanent() != null) {
                    entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
                }
            }
            if (match.permanent() != null && containsChosenPlayerMill(resolved)) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            // "For each of the chosen colors it is" reads the source's choice as it resolves, as the source last
            // existed once it has left the battlefield (CR 608.2h).
            if (match.permanent() != null && resolved.stream()
                    .anyMatch(effect -> SourcePermanentSnapshotRequiredEffect.isRequiredBy(effect)
                            || effectReadsSourceChosenColors(effect))) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            preservePlanarSource(entry, match);
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
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
     * Whether {@code effect}'s graveyard target is restricted by the X the ability goes on the stack with ("return target
     * creature card with mana value equal to that spell's mana value", Krile Baldesion): it is chosen now, as the ability
     * goes on the stack (CR 603.3d), so the choice takes the spell's mana value as its X.
     */
    private static boolean graveyardTargetReadsManaValueX(CardEffect effect) {
        return switch (effect) {
            case ReturnCardFromGraveyardEffect returnEffect -> returnEffect.targetGraveyard()
                    && (returnEffect.requiresManaValueEqualsX() || returnEffect.requiresManaValueAtMostX()
                    || readsManaValueX(returnEffect.filter()));
            case ConditionalEffect conditional -> graveyardTargetReadsManaValueX(conditional.wrapped());
            case MayEffect may -> graveyardTargetReadsManaValueX(may.wrapped());
            case SequenceEffect sequence -> sequence.steps().stream()
                    .anyMatch(SpellCastTriggerCollectorService::graveyardTargetReadsManaValueX);
            default -> false;
        };
    }

    private static boolean readsManaValueX(CardPredicate predicate) {
        return switch (predicate) {
            case CardManaValueEqualsXPredicate ignored -> true;
            case CardMaxManaValueXPredicate ignored -> true;
            case CardManaValueLessThanXPredicate ignored -> true;
            case CardAllOfPredicate allOf -> allOf.predicates().stream()
                    .anyMatch(SpellCastTriggerCollectorService::readsManaValueX);
            case CardAnyOfPredicate anyOf -> anyOf.predicates().stream()
                    .anyMatch(SpellCastTriggerCollectorService::readsManaValueX);
            case CardNotPredicate not -> readsManaValueX(not.predicate());
            case null, default -> false;
        };
    }

    /** Whether {@code effect} counts the triggering spell's colors among the colors its source chose. */
    private static boolean effectReadsSourceChosenColors(CardEffect effect) {
        return switch (effect) {
            case GainLifeEffect gainLife -> gainLife.amount() instanceof TriggeringSpellChosenColorCount;
            case ConditionalEffect conditional -> effectReadsSourceChosenColors(conditional.wrapped());
            case MayEffect may -> effectReadsSourceChosenColors(may.wrapped());
            case SequenceEffect sequence -> sequence.steps().stream()
                    .anyMatch(SpellCastTriggerCollectorService::effectReadsSourceChosenColors);
            default -> false;
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
        // permanent or the face-up plane (The Matrix of Time), which the cast event names since the card has left exile.
        if (predicate instanceof StackEntryIsCardExiledWithSourcePredicate) {
            UUID sourceId = match.permanent() != null ? match.permanent().getId()
                    : match.sourcePlanarObject() != null ? match.sourcePlanarObject().getId() : null;
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

    private boolean containsChosenPlayerMill(List<CardEffect> effects) {
        return effects.stream().anyMatch(effect -> effect instanceof MillEffect mill
                && mill.recipient() == MillRecipient.CHOSEN_PLAYER);
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

    @CollectsSpellAbilityTrigger(value = ModalSpellCastTriggerEffect.class, watch = SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleModalSpellCastTrigger(TriggerMatchContext match,
            ModalSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast spellCast = (TriggerContext.SpellCast) ctx;
        StackEntry triggeringSpell = findStackEntryForCard(match.gameData(), spellCast.spellCard().getId());
        if (triggeringSpell == null || triggeringSpell.getModalModeCount() == null
                || triggeringSpell.getModalModeCount() <= 0) {
            return false;
        }

        int choices = Math.min(triggeringSpell.getModalModeCount(), trigger.options().size());
        ChooseOneEffect modalChoice = new ChooseOneEffect(trigger.options(), true, 0, choices);
        match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                match.permanent().getCard(), match.controllerId(), modalChoice,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
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
