package com.github.laxika.magicalvibes.service.trigger;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachPriorInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.FirstSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerOtherThanPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@ExtendWith(MockitoExtension.class)
class SpellCastTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private AmountEvaluationService amountEvaluationService;
    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private TargetLegalityService targetLegalityService;

    @InjectMocks
    private SpellCastTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private static Card createCard(String name, CardColor color) {
        Card card = createCard(name);
        card.setColor(color);
        return card;
    }

    private static Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    /** The ability of {@code trigger}, a filtered cast trigger, and {@code effects}, as the spell-cast collector judges it. */
    private static CastAbility castAbility(SpellTrigger trigger, CardEffect... effects) {
        return CastAbility.of(new TriggeredAbility(trigger, effects), trigger.filter());
    }

    /** The cast ability a {@link SpellCastTriggerEffect}'s fields describe, as the spell-cast collector judges it. */
    private static CastAbility authored(SpellCastTriggerEffect trigger) {
        SpellFilter.Turn turn = trigger.onlyDuringOpponentTurn() ? SpellFilter.Turn.OPPONENTS
                : trigger.onlyDuringControllerTurn() ? SpellFilter.Turn.YOURS
                : trigger.onlyWhenCasterNotActiveTurn() ? SpellFilter.Turn.NOT_CASTERS
                : SpellFilter.Turn.ANY;
        SpellFilter.Kicker kicker = !trigger.kicked() ? SpellFilter.Kicker.ANY
                : trigger.triggerPerKick() ? SpellFilter.Kicker.EACH_KICK : SpellFilter.Kicker.KICKED;
        SpellFilter filter = new SpellFilter(trigger.spellFilter(), trigger.castSpellTargetCondition(), turn,
                trigger.onlyDuringCombat(), trigger.nthSpellNumber(), trigger.minimumSpellNumber(),
                trigger.requiresManaProducedBySource(), trigger.requiresTreasureMana(),
                trigger.requiresTwoOrMoreCardTypes(), trigger.expendThreshold(), trigger.castFrom(),
                trigger.notCastFrom(), kicker, trigger.countEveryPlayersSpells());
        return new CastAbility(filter, trigger.resolvedEffects(), trigger.intervening(), trigger.targetFilter());
    }

    /** Every spell matches every spell filter the collector checks, as cast and in the turn's spell history. */
    private void everySpellMatches() {
        lenient().when(predicateEvaluationService.matchesCardPredicate(
                any(Card.class), any(), any(), eq(gd), any(UUID.class), isNull(), isNull(), any()))
                .thenReturn(true);
        lenient().when(predicateEvaluationService.matchesCardPredicate(
                any(Card.class), any(), isNull(), eq(gd), any(UUID.class)))
                .thenReturn(true);
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — FirstSpellCastTriggerEffect")
    class FirstSpellCastTrigger {

        @Test
        @DisplayName("tracks the first spell independently for each player and excludes its caster")
        void tracksEachPlayerIndependentlyAndExcludesCaster() {
            Permanent perm = createPermanent("The Lord of Pain");
            var effect = new FirstSpellCastTriggerEffect(List.of(
                    new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER)));
            Card player1Spell = createCard("Player 1 Spell");
            player1Spell.setManaCost("{1}{G}");
            Card player2Spell = createCard("Player 2 Spell");
            player2Spell.setManaCost("{2}");
            when(predicateEvaluationService.matchesCardPredicate(
                    any(Card.class), isNull(), any(UUID.class), eq(gd), any(UUID.class), isNull(), isNull(), any()))
                    .thenReturn(true);

            gd.recordSpellCast(player1Id, player1Spell);
            boolean player1Result = registry.dispatch(
                    match(perm, player1Id, effect), SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(player1Spell, player1Id, true));

            gd.recordSpellCast(player2Id, player2Spell);
            boolean player2Result = registry.dispatch(
                    match(perm, player1Id, effect), SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(player2Spell, player2Id, true));

            assertThat(player1Result).isTrue();
            assertThat(player2Result).isTrue();
            var pending = gd.pendingInteractions.stream()
                    .filter(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .map(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::cast)
                    .toList();
            assertThat(pending).hasSize(2);
            assertThat(pending).extracting(PermanentChoiceContext.SpellTargetTriggerAnyTarget::triggeringSpellManaValue)
                    .containsExactly(2, 2);
            assertThat(pending).allSatisfy(queued -> {
                assertThat(queued.targetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
                var filter = (PlayerPredicateTargetFilter) queued.targetFilter();
                assertThat(filter.predicate()).isInstanceOf(PlayerOtherThanPredicate.class);
            });
            assertThat(pending).anySatisfy(queued -> assertThat(
                    ((PlayerOtherThanPredicate) ((PlayerPredicateTargetFilter) queued.targetFilter()).predicate())
                            .excludedPlayerId()).isEqualTo(player1Id));
            assertThat(pending).anySatisfy(queued -> assertThat(
                    ((PlayerOtherThanPredicate) ((PlayerPredicateTargetFilter) queued.targetFilter()).predicate())
                            .excludedPlayerId()).isEqualTo(player2Id));
        }

        @Test
        @DisplayName("does not trigger for a player's later spell")
        void ignoresLaterSpell() {
            Permanent perm = createPermanent("The Lord of Pain");
            var effect = new FirstSpellCastTriggerEffect(List.of(
                    new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER)));
            Card firstSpell = createCard("First Spell");
            firstSpell.setManaCost("{1}");
            Card secondSpell = createCard("Second Spell");
            secondSpell.setManaCost("{2}");
            when(predicateEvaluationService.matchesCardPredicate(
                    any(Card.class), isNull(), any(UUID.class), eq(gd), any(UUID.class), isNull(), isNull(), any()))
                    .thenReturn(true);

            gd.recordSpellCast(player1Id, firstSpell);
            registry.dispatch(match(perm, player1Id, effect), SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(firstSpell, player1Id, true));
            gd.recordSpellCast(player1Id, secondSpell);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect), SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(secondSpell, player1Id, true));

            assertThat(result).isFalse();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — single-target creature control trigger")
    class SingleTargetCreatureControlTrigger {

        @Test
        @DisplayName("binds the triggering spell's sole creature target")
        void bindsSoleCreatureTarget() {
            Permanent source = createPermanent("Loki, God of Lies");
            Permanent target = createPermanent("Grizzly Bears");
            Card spell = createInstant("Giant Growth");
            gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id,
                    spell.getName(), List.of(), 0, target.getId(), java.util.Map.of()));
            var effect = new GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect();

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);

            boolean result = registry.dispatch(match(source, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(spell, player1Id, true));

            assertThat(result).isTrue();
            StackEntry triggeredAbility = gd.stack.getLast();
            assertThat(triggeredAbility.getTargetId()).isEqualTo(target.getId());
            assertThat(triggeredAbility.isNonTargeting()).isTrue();
            assertThat(triggeredAbility.getTriggeringCardId()).isEqualTo(spell.getId());
            assertThat(triggeredAbility.getEffectsToResolve()).hasSize(2);
            assertThat(triggeredAbility.getEffectsToResolve().getFirst())
                    .isEqualTo(new GainControlOfTargetEffect(ControlDuration.END_OF_TURN));
            ConditionalEffect rider = (ConditionalEffect) triggeredAbility.getEffectsToResolve().getLast();
            assertThat(rider.wrapped()).isInstanceOf(SequenceEffect.class);
            SequenceEffect sequence = (SequenceEffect) rider.wrapped();
            assertThat(sequence.steps()).containsExactly(
                    new UntapPermanentsEffect(com.github.laxika.magicalvibes.model.effect.TapUntapScope.TARGET),
                    new GrantKeywordEffect(com.github.laxika.magicalvibes.model.Keyword.HASTE,
                            com.github.laxika.magicalvibes.model.effect.GrantScope.TARGET));
        }

        @Test
        @DisplayName("does not trigger for multiple distinct targets")
        void doesNotTriggerForMultipleDistinctTargets() {
            Permanent source = createPermanent("Loki, God of Lies");
            Permanent firstTarget = createPermanent("First creature");
            Permanent secondTarget = createPermanent("Second creature");
            Card spell = createInstant("Two-target spell");
            gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, spell, player1Id,
                    spell.getName(), List.of(), null,
                    List.of(firstTarget.getId(), secondTarget.getId())));
            var effect = new GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect();

            boolean result = registry.dispatch(match(source, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect,
                    new TriggerContext.SpellCast(spell, player1Id, true));

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Test
    void referringToTheTriggeringSpellDoesNotMakeItADeclaredTarget() {
        Permanent source = createPermanent("Spell watcher");
        Card spell = createInstant("Triggering spell");
        var effect = new SpellCastTriggerEffect(null, List.of(new BoostSelfEffect(2, 2),
                new MayEffect(new com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect(),
                        "Choose new targets?")));
        var context = new TriggerContext.SpellCast(spell, player1Id, true);
        when(predicateEvaluationService.matchesCardPredicate(eq(spell), eq(null),
                eq(source.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

        sut.collectCastAbility(match(source, player1Id, effect), authored(effect), context, true);

        assertThat(gd.stack).hasSize(1);
        StackEntry triggeredAbility = gd.stack.getLast();
        assertThat(triggeredAbility.isNonTargeting()).isTrue();
        assertThat(triggeredAbility.getTriggeringCardId()).isEqualTo(spell.getId());
    }

    @Test
    void spellCastTriggerPreservesChosenX() {
        Permanent source = createPermanent("X source");
        Card spell = createInstant("X spell");
        StackEntry spellEntry = new StackEntry(StackEntryType.INSTANT_SPELL, spell,
                player1Id, "X spell", List.of(), 3, (UUID) null);
        gd.stack.add(spellEntry);
        var effect = new SpellCastTriggerEffect(null, List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(new XValue())));
        when(predicateEvaluationService.matchesCardPredicate(spell, null,
                source.getOriginalCard().getId(), gd, player1Id, null, null, 3)).thenReturn(true);

        sut.collectCastAbility(match(source, player1Id, effect), authored(effect),
                new TriggerContext.SpellCast(spell, player1Id, true), true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getXValue()).isEqualTo(3);
    }

    @Test
    void secondSpellDamageRequestsATargetBeforeStacking() {
        Permanent perm = createPermanent("Second spell source");
        Card spell = createInstant("Spell");
        gd.recordSpellCast(player1Id, spell);
        gd.recordSpellCast(player1Id, spell);
        everySpellMatches();
        var effect = castAbility(Triggers.youCastSpell().nthThisTurn(2), new DealDamageToAnyTargetEffect(2));

        boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                effect, new TriggerContext.SpellCast(spell, player1Id, true), true);

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class))
                .isNotNull();
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — a cast trigger's event condition")
    class CastEventCondition {

        private CastAbility whileAttacking() {
            return CastAbility.of(new TriggeredAbility(Triggers.youCastSpell().anySpell(),
                    TriggerOptions.DEFAULT.withEventCondition(new SourceIsAttacking()),
                    new CopyTriggeringSpellEffect()), SpellFilter.ANY);
        }

        @Test
        @DisplayName("triggers when the event condition holds as the spell is cast, and doesn't check it again")
        void triggersWhileEventConditionHolds() {
            Permanent perm = createPermanent("Fire Lord Azula");
            Card spellCard = createInstant("Life Gain");
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            gd.stack.add(spellOnStack);
            everySpellMatches();
            when(conditionEvaluationService.isMet(eq(gd), eq(new SourceIsAttacking()), any())).thenReturn(true);
            CastAbility ability = whileAttacking();

            boolean result = sut.collectCastAbility(match(perm, player1Id, ability.effects().getFirst()), ability,
                    new TriggerContext.SpellCast(spellCard, player1Id, true), true);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            StackEntry trigger = gd.stack.getLast();
            assertThat(trigger.getEffectsToResolve()).containsExactly(new CopyTriggeringSpellEffect());
            assertThat(trigger.getTriggeringCardId()).isEqualTo(spellCard.getId());
            assertThat(trigger.getTriggeringSpellSnapshot().getCard()).isSameAs(spellCard);
        }

        @Test
        @DisplayName("does not trigger when the event condition is false as the spell is cast")
        void doesNotTriggerWhenEventConditionFails() {
            Permanent perm = createPermanent("Fire Lord Azula");
            Card spellCard = createInstant("Life Gain");
            gd.stack.add(new StackEntry(spellCard, player1Id));
            everySpellMatches();
            when(conditionEvaluationService.isMet(eq(gd), eq(new SourceIsAttacking()), any())).thenReturn(false);
            CastAbility ability = whileAttacking();

            boolean result = sut.collectCastAbility(match(perm, player1Id, ability.effects().getFirst()), ability,
                    new TriggerContext.SpellCast(spellCard, player1Id, true), true);

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — SpellCastTriggerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — SpellCastTriggerEffect")
    class AnyPlayerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Shrine of Burning Rage");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Shrine of Burning Rage");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("caster-relative turn gate ignores spells cast by the active player")
        void casterRelativeTurnGateUsesCastingPlayer() {
            Permanent perm = createPermanent("Vision");
            var effect = SpellCastTriggerEffect.whenCasterIsNotActiveTurn(
                    List.of(new BoostSelfEffect(1, 1)));
            Card spellCard = createInstant("Opt");
            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            gd.activePlayerId = player1Id;
            boolean activeCasterResult = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(spellCard, player1Id, true), false);

            assertThat(activeCasterResult).isFalse();
            assertThat(gd.stack).isEmpty();

            boolean nonActiveCasterResult = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(spellCard, player2Id, true), false);

            assertThat(nonActiveCasterResult).isTrue();
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Shrine of Burning Rage");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = new SpellCastTriggerEffect(filter, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(false);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, false);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("binds a spell target to the spell that caused the trigger")
        void bindsSpellTargetToTriggeringSpell() {
            Permanent perm = createPermanent("Presence of the Master");
            var effect = new SpellCastTriggerEffect(null, List.of(new CounterSpellEffect()));
            Card spellCard = createCard("Angelic Chorus");
            StackEntry spellOnStack = new StackEntry(spellCard, player2Id);
            gd.stack.add(spellOnStack);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            StackEntry triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(triggerEntry.getTargetZone()).isEqualTo(Zone.STACK);
        }

        @Test
        @DisplayName("uses self-targeting stack entry when resolved effect is self-targeting")
        void selfTargetingStackEntry() {
            Permanent perm = createPermanent("Some Permanent");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            sut.collectCastAbility(match(perm, player1Id, effect), authored(effect), ctx, false);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player1Id);
            assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("captures the attached permanent for an attached self-targeting effect")
        void attachedSelfTargetingStackEntry() {
            Permanent aura = createPermanent("Predatory Hunger");
            Permanent host = createPermanent("Enchanted Creature");
            aura.setAttachedTo(host.getId());
            var innerEffect = new PutCountersOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(aura.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            sut.collectCastAbility(match(aura, player1Id, effect), authored(effect), ctx, false);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(host.getId());
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(aura.getId());
        }

        @Test
        @DisplayName("snapshots spell mana spent into xValue when a resolved amount references X")
        void snapshotsSpellManaSpentForXValueAmounts() {
            Permanent perm = createPermanent("Aberrant Manawurm");
            var innerEffect = new BoostSelfEffect(new XValue(), new Fixed(0));
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);
            when(amountEvaluationService.referencesXValue(new XValue())).thenReturn(true);
            gd.addSpellCastManaSpent(spellCard.getId(), 3);

            sut.collectCastAbility(match(perm, player1Id, effect), authored(effect), ctx, false);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getXValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("snapshots spell mana spent when a token's power references X")
        void snapshotsSpellManaSpentForTokenPower() {
            Permanent perm = createPermanent("Manaform Hellkite");
            var token = new CreateTokenEffect(
                    "Dragon Illusion", new XValue(), new XValue(), CardColor.RED,
                    List.of(CardSubtype.DRAGON, CardSubtype.ILLUSION), Set.of(), Set.of());
            var innerEffect = new CreateTokenForTriggeringPlayerEffect(token);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);
            when(amountEvaluationService.referencesXValue(new Fixed(1))).thenReturn(false);
            when(amountEvaluationService.referencesXValue(new XValue())).thenReturn(true);
            gd.addSpellCastManaSpent(spellCard.getId(), 3);

            sut.collectCastAbility(match(perm, player1Id, effect), authored(effect), ctx, false);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getXValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("snapshots triggering spell mana value for EventValue counter amounts")
        void snapshotsTriggeringSpellManaValueForEventValueCounterAmounts() {
            Permanent perm = createPermanent("Chrome Host Seedshark");
            var innerEffect = new CreateXTokenWithXCountersEffect(
                    CreateTokenEffect.whiteSpirit(1), new EventValue(), CounterType.PLUS_ONE_PLUS_ONE);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createInstant("Divination");
            spellCard.setManaCost("{2}{U}");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);
            sut.collectCastAbility(match(perm, player1Id, effect), authored(effect), ctx, true);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEventValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("puts an optional trigger on the stack, its choice made as it resolves (CR 603.5)")
        void putsOptionalTriggerOnStackWhenMayEffect() {
            Permanent perm = createPermanent("Angel's Feather");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var mayEffect = new MayEffect(innerEffect, "Gain 1 life?");
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            everySpellMatches();
            CastAbility ability = castAbility(Triggers.playerCastsSpell().anySpell(), mayEffect);

            boolean result = sut.collectCastAbility(match(perm, player1Id, mayEffect), ability, ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).singleElement().satisfies(entry -> assertThat(entry.getEffectsToResolve())
                    .containsExactly(new MayEffect(innerEffect, "Gain 1 life?")));
        }

        @Test
        @DisplayName("an optional trigger whose effect reads the mana spent also goes on the stack, asked as it resolves")
        void optionalManaSpentTriggerGoesOnStack() {
            Permanent perm = createPermanent("Uncover the Moon-Letters");
            var draw = new DrawCardEffect(new XValue());
            var mayEffect = new MayEffect(draw, "Draw cards?");
            Card spellCard = createInstant("Dark Ritual");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            gd.addSpellCastManaSpent(spellCard.getId(), 3);
            everySpellMatches();
            when(amountEvaluationService.referencesXValue(new XValue())).thenReturn(true);
            CastAbility ability = castAbility(Triggers.playerCastsSpell().anySpell(), mayEffect);

            boolean result = sut.collectCastAbility(match(perm, player1Id, mayEffect), ability, ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(new MayEffect(draw, "Draw cards?"));
                assertThat(entry.getXValue()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("a targeting trigger takes its target filter from the target group its effect is bound to")
        void targetFilterComesFromTheBoundTargetGroup() {
            Card source = createCard("Necklace of Girion");
            source.target(TargetFilters.creature(), 0, 1)
                    .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
            source.target(TargetFilters.creatureYouControl())
                    .addTrigger(Triggers.youCastSpell().anySpell(), new BoostTargetCreatureEffect(1, 1));
            Permanent perm = new Permanent(source);
            TriggeredAbility ability = source.getTriggeredAbilities().get(1);
            CastAbility effect = CastAbility.of(ability, SpellFilter.ANY);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()), effect, ctx,
                    true);

            // The ability's own group takes one target, so it is not the card's "up to one" multi-target choice.
            assertThat(result).isTrue();
            assertThat(gd.pendingInteractions).singleElement()
                    .isInstanceOfSatisfying(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class, queued ->
                            assertThat(queued.targetFilter()).isSameAs(source.getTargetGroups().get(1).getFilter()));
        }

        @Test
        @DisplayName("a \"you may pay\" targeting trigger queues the target choice with the payment as written")
        void targetingTriggerWithManaCostKeepsItsMayPayMana() {
            Permanent perm = createPermanent("Malachite Talisman");
            var innerEffect = new UntapPermanentsEffect(TapUntapScope.TARGET);
            var pay = new MayPayManaEffect("{3}", innerEffect, "Pay {3}?");
            var ability = new CastAbility(SpellFilter.ANY, List.of(pay), null, TargetFilters.permanent());
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, pay), ability, ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
            var queued = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) gd.pendingInteractions.getFirst();
            assertThat(queued.controllerId()).isEqualTo(player1Id);
            assertThat(queued.effects()).containsExactly(pay);
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CounterSpellIfManaValueEqualsSourceCountersEffect")
    class AnyPlayerSourceCounterCounter {

        @Test
        @DisplayName("qualifies and targets the triggering spell using its cast-time mana value")
        void qualifiesAndTargetsTriggeringSpell() {
            Permanent perm = createPermanent("Chalice of the Void");
            perm.setCounterCount(CounterType.CHARGE, 2);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            spellCard.setManaCost("{1}{G}");
            StackEntry spellEntry = new StackEntry(
                    StackEntryType.CREATURE_SPELL, spellCard, player2Id, spellCard.getName(), List.of(), 0);
            gd.stack.add(spellEntry);
            var effect = new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(targetLegalityService.matchesStackEntryPredicate(
                    eq(gd), eq(spellEntry), any(), eq(player1Id), eq(perm))).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            StackEntry triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(triggerEntry.getTargetZone()).isEqualTo(Zone.STACK);
            assertThat(triggerEntry.getEffectsToResolve()).singleElement()
                    .isEqualTo(new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE, 2));
        }
        @Test
        @DisplayName("snapshots every X symbol in the triggering spell mana value")
        void snapshotsBothXSymbolsInTriggeringSpellManaValue() {
            Permanent perm = createPermanent("Chalice of the Void");
            perm.setCounterCount(CounterType.CHARGE, 4);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            spellCard.setManaCost("{X}{X}");
            StackEntry spellEntry = new StackEntry(
                    StackEntryType.CREATURE_SPELL, spellCard, player2Id, spellCard.getName(), List.of(), 2);
            gd.stack.add(spellEntry);
            var effect = new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(targetLegalityService.matchesStackEntryPredicate(
                    eq(gd), eq(spellEntry), any(), eq(player1Id), eq(perm))).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            StackEntry triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(triggerEntry.getTargetZone()).isEqualTo(Zone.STACK);
            assertThat(triggerEntry.getEffectsToResolve()).singleElement()
                    .isEqualTo(new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE, 4));
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CasterLosesLifeOnChosenColorSpellCastEffect")
    class AnyPlayerChosenColorLifeLoss {

        @Test
        @DisplayName("puts a life-loss trigger on the stack for a matching spell")
        void triggersForMatchingColor() {
            Permanent perm = createPermanent("Curse of Wizardry");
            perm.setChosenColor(CardColor.RED);
            var effect = new CasterLosesLifeOnChosenColorSpellCastEffect(1);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            spellCard.setColors(List.of(CardColor.RED));
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("does not trigger for a spell of another color")
        void ignoresAnotherColor() {
            Permanent perm = createPermanent("Curse of Wizardry");
            perm.setChosenColor(CardColor.RED);
            var effect = new CasterLosesLifeOnChosenColorSpellCastEffect(1);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class AnyPlayerColorCounter {

        @Test
        @DisplayName("triggers when spell color matches and onlyOwnSpells is false")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("stack entry includes PutCountersOnSourceEffect with correct amount")
        void stackEntryIncludesCorrectEffect() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 2, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            var resolved = (PutCountersOnSourceEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.amount()).isEqualTo(2);
        }

        @Test
        @DisplayName("empty color set triggers on a colorless spell (Managorger Hydra)")
        void emptyColorSetTriggersOnColorlessSpell() {
            Permanent perm = createPermanent("Managorger Hydra");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false);
            Card spellCard = createCard("Artifact Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("returns false when spell color is null")
        void returnsFalseWhenColorNull() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Artifact Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("returns false when spell color does not match trigger colors")
        void returnsFalseWhenColorDoesNotMatch() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("returns false when onlyOwnSpells is true")
        void returnsFalseWhenOnlyOwnSpells() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, true);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — KnowledgePoolCastTriggerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — KnowledgePoolCastTriggerEffect")
    class AnyPlayerKnowledgePool {

        @Test
        @DisplayName("puts triggered ability on stack when cast from hand")
        void triggersWhenCastFromHand() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Knowledge Pool");
        }

        @Test
        @DisplayName("stack entry contains KnowledgePoolExileAndCastEffect with correct IDs")
        void stackEntryContainsCorrectEffect() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            var resolved = (KnowledgePoolExileAndCastEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(resolved.originalSpellCardId()).isEqualTo(spellCard.getId());
            assertThat(resolved.knowledgePoolPermanentId()).isEqualTo(perm.getId());
            assertThat(resolved.castingPlayerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns false when not cast from hand")
        void returnsFalseWhenNotCastFromHand() {
            Permanent perm = createPermanent("Knowledge Pool");
            var effect = new KnowledgePoolCastTriggerEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherControlledCreatureEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherControlledCreatureEffect")
    class AnyPlayerCopySpellForEachOtherControlledCreature {

        @Test
        @DisplayName("puts triggered ability on stack when spell targets only the source permanent")
        void triggersWhenSpellTargetsOnlySource() {
            Permanent dragon = createPermanent("Mirrorwing Dragon");
            var effect = new CopySpellForEachOtherControlledCreatureEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(dragon.getId());
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(dragon, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when spell targets a different permanent")
        void returnsFalseWhenTargetIsNotSource() {
            Permanent dragon = createPermanent("Mirrorwing Dragon");
            Permanent other = createPermanent("Grizzly Bears");
            var effect = new CopySpellForEachOtherControlledCreatureEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(other.getId());
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(dragon, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when trigger already has a spell snapshot")
        void returnsFalseWhenSnapshotNotNull() {
            Permanent dragon = createPermanent("Mirrorwing Dragon");
            var snapshot = new StackEntry(createCard("Dummy"), player1Id);
            var effect = new CopySpellForEachOtherControlledCreatureEffect(
                    snapshot, player1Id, dragon.getId());
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(dragon, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherCreatureEffect")
    class AnyPlayerCopySpellForEachOtherCreature {

        @Test
        @DisplayName("puts triggered ability on stack when spell targets only the source permanent")
        void triggersWhenSpellTargetsOnlySource() {
            Permanent source = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherCreatureEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player2Id);
            spellOnStack.setTargetId(source.getId());
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when spell targets a different permanent")
        void returnsFalseWhenTargetIsNotSource() {
            Permanent source = createPermanent("Ink-Treader Nephilim");
            Permanent other = createPermanent("Grizzly Bears");
            var effect = new CopySpellForEachOtherCreatureEffect();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player2Id);
            spellOnStack.setTargetId(other.getId());
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherSubtypePermanentEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherSubtypePermanentEffect")
    class AnyPlayerCopySpellForSubtype {

        @Test
        @DisplayName("puts triggered ability on stack for instant targeting matching subtype permanent")
        void triggersForInstantTargetingSubtype() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Put the spell on the stack with a single target
            Permanent targetPerm = createPermanent("Goblin Guide");
            TestCards.mutableCard(targetPerm).setSubtypes(List.of(CardSubtype.GOBLIN));
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(targetPerm.getId());
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, targetPerm.getId())).thenReturn(targetPerm);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            // Stack now has the original spell + the triggered ability
            assertThat(gd.stack).hasSize(2);
            var triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when trigger already has a spell snapshot")
        void returnsFalseWhenSnapshotNotNull() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var snapshot = new StackEntry(createCard("Dummy"), player1Id);
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(
                    CardSubtype.GOBLIN, snapshot, player1Id, UUID.randomUUID());
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell is not an instant or sorcery")
        void returnsFalseWhenNotInstantOrSorcery() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createCard("Grizzly Bears"); // creature
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell is not found on the stack")
        void returnsFalseWhenSpellNotOnStack() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Stack is empty — spell not found
            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when single target is a player ID")
        void returnsFalseWhenTargetIsPlayer() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(player2Id);
            gd.stack.add(spellOnStack);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when target permanent is not found on the battlefield")
        void returnsFalseWhenTargetPermanentNotFound() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            UUID missingPermanentId = UUID.randomUUID();
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(missingPermanentId);
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, missingPermanentId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when target permanent does not have matching subtype")
        void returnsFalseWhenSubtypeDoesNotMatch() {
            Permanent perm = createPermanent("Ink-Treader Nephilim");
            var effect = new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOBLIN);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            Permanent targetPerm = createPermanent("Llanowar Elves");
            // No GOBLIN subtype
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setTargetId(targetPerm.getId());
            gd.stack.add(spellOnStack);

            when(gameQueryService.findPermanentById(gd, targetPerm.getId())).thenReturn(targetPerm);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherPlayerEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — CopySpellForEachOtherPlayerEffect")
    class AnyPlayerCopySpellForEachOtherPlayer {

        private static CopySpellForEachOtherPlayerEffect instantSorceryCopy() {
            return new CopySpellForEachOtherPlayerEffect(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)));
        }

        @Test
        @DisplayName("puts triggered ability on stack when the spell is on the stack and the filter matches")
        void triggersWhenFilterMatches() {
            Permanent perm = createPermanent("Hive Mind");
            var effect = instantSorceryCopy();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(
                    StackEntryType.INSTANT_SPELL, spellCard, player1Id, "Lightning Bolt", new ArrayList<>());
            gd.stack.add(spellOnStack);
            when(predicateEvaluationService.matchesStackEntryPredicate(eq(spellOnStack), eq(effect.spellFilter()), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            var triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("returns false when spell is not found on the stack")
        void returnsFalseWhenSpellNotOnStack() {
            Permanent perm = createPermanent("Hive Mind");
            var effect = instantSorceryCopy();
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            // Stack is empty — spell not found
            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when trigger already has a spell snapshot")
        void returnsFalseWhenSnapshotNotNull() {
            Permanent perm = createPermanent("Hive Mind");
            var snapshot = new StackEntry(createCard("Dummy"), player1Id);
            var effect = new CopySpellForEachOtherPlayerEffect(snapshot, player1Id);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when the spell filter rejects the cast (e.g. a creature spell)")
        void returnsFalseWhenFilterRejects() {
            Permanent perm = createPermanent("Hive Mind");
            var effect = instantSorceryCopy();
            Card spellCard = createCard("Grizzly Bears"); // creature
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            StackEntry spellOnStack = new StackEntry(
                    StackEntryType.CREATURE_SPELL, spellCard, player1Id, "Grizzly Bears", new ArrayList<>());
            gd.stack.add(spellOnStack);
            when(predicateEvaluationService.matchesStackEntryPredicate(eq(spellOnStack), eq(effect.spellFilter()), any()))
                    .thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("A same-name graveyard life trigger binds the caster as a non-target")
    void sameNameGraveyardLifeTriggerBindsCaster() {
        Permanent perm = createPermanent("Aven Shrine");
        var effect = new GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("A same-name graveyard Squirrel trigger binds the caster as a non-target")
    void sameNameGraveyardSquirrelTriggerBindsCaster() {
        Permanent perm = createPermanent("Nantuko Shrine");
        var effect = new CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        assertThat(gd.stack.getLast().getEffectsToResolve()).singleElement()
                .isEqualTo(new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                        new CardsInGraveyard(new CardNamedPredicate("Grizzly Bears"), CountScope.ANY_PLAYER),
                        "Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of())));
    }

    @Test
    @DisplayName("A same-name graveyard return trigger only fires for creature spells")
    void sameNameGraveyardReturnTriggerSnapshotsCreatureSpellName() {
        Permanent perm = createPermanent("Bloodbond March");
        var effect = new ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect();
        Card spellCard = createCard("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        assertThat(gd.stack.getLast().getEffectsToResolve()).singleElement()
                .isEqualTo(new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE, new CardNamedPredicate("Grizzly Bears")));

        gd.stack.clear();
        Card noncreatureSpell = createInstant("Holy Day");
        var noncreatureContext = new TriggerContext.SpellCast(noncreatureSpell, player2Id, true);

        assertThat(registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, noncreatureContext)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A same-name graveyard damage trigger binds the caster as a non-target")
    void sameNameGraveyardDamageTriggerBindsCaster() {
        Permanent perm = createPermanent("Dwarven Shrine");
        var effect = new DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        assertThat(gd.stack.getLast().getEffectsToResolve()).singleElement()
                .isEqualTo(new DealDamageToPlayersEffect(
                        new Scaled(new CardsInGraveyard(
                                new CardNamedPredicate("Grizzly Bears"), CountScope.ANY_PLAYER), 2),
                        DamageRecipient.TRIGGERING_PLAYER));
    }

    @Test
    @DisplayName("A same-name graveyard counter trigger snapshots the spell name without targeting it")
    void sameNameGraveyardCounterTriggerSnapshotsSpellName() {
        Permanent perm = createPermanent("Cephalid Shrine");
        var effect = new CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        assertThat(gd.stack.getLast().getEffectsToResolve()).singleElement()
                .isEqualTo(new CounterUnlessPaysEffect(
                        new CardsInGraveyard(new CardNamedPredicate("Grizzly Bears"), CountScope.ANY_PLAYER)));
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class ControllerColorCounter {

        @Test
        @DisplayName("triggers when spell color matches trigger colors")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("returns false when spell color is null")
        void returnsFalseWhenColorNull() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Artifact Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell color is not in trigger colors (e.g. green for Quirion Dryad)")
        void returnsFalseWhenColorNotInTriggerColors() {
            Permanent perm = createPermanent("Quirion Dryad");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED), 1, true);
            Card spellCard = createCard("Giant Growth", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — SpellCastTriggerEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — SpellCastTriggerEffect")
    class ControllerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter (non-targeting)")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Cabal Paladin");
            var innerEffect = new PutCountersOnSourceEffect(1, 1, 1);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("fires only on the Nth spell matching its filter")
        void firesOnlyOnNthMatchingSpell() {
            Permanent perm = createPermanent("Vengevine");
            CardPredicate filter = new CardTypePredicate(CardType.CREATURE);
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = SpellCastTriggerEffect.nth(2, filter, List.of(innerEffect));
            Card noncreatureSpell = createInstant("Spellbook");
            Card firstCreatureSpell = createCard("Grizzly Bears");
            Card secondCreatureSpell = createCard("Llanowar Elves");
            gd.recordSpellCast(player1Id, noncreatureSpell);
            gd.recordSpellCast(player1Id, firstCreatureSpell);
            gd.recordSpellCast(player1Id, secondCreatureSpell);
            var ctx = new TriggerContext.SpellCast(secondCreatureSpell, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(
                    any(Card.class), eq(filter), any(), eq(gd), eq(player1Id), isNull(), isNull(), any()))
                    .thenAnswer(invocation -> invocation.getArgument(0, Card.class).hasType(CardType.CREATURE));

            when(predicateEvaluationService.matchesCardPredicate(
                    any(Card.class), eq(filter), isNull(), eq(gd), eq(player1Id)))
                    .thenAnswer(invocation -> invocation.getArgument(0, Card.class).hasType(CardType.CREATURE));

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("fires on the second and every later matching spell during the controller's turn")
        void firesOnSecondAndLaterMatchingSpells() {
            Permanent perm = createPermanent("Geralf, the Fleshwright");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = SpellCastTriggerEffect.atLeastDuringYourTurn(2, null, List.of(innerEffect));
            Card firstSpell = createInstant("Opt");
            Card secondSpell = createInstant("Consider");
            Card thirdSpell = createInstant("Brainstorm");
            gd.activePlayerId = player1Id;

            when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(null), any(),
                    eq(gd), eq(player1Id), isNull(), isNull(), any())).thenReturn(true);

            when(predicateEvaluationService.matchesCardPredicate(any(Card.class), isNull(), isNull(),
                    eq(gd), eq(player1Id))).thenReturn(true);

            gd.recordSpellCast(player1Id, firstSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(firstSpell, player1Id, true), true)).isFalse();

            gd.recordSpellCast(player1Id, secondSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(secondSpell, player1Id, true), true)).isTrue();

            gd.recordSpellCast(player1Id, thirdSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(thirdSpell, player1Id, true), true)).isTrue();
            assertThat(gd.stack).hasSize(2);
        }

        @Test
        @DisplayName("does not fire the own-turn minimum spell trigger during an opponent's turn")
        void doesNotFireDuringOpponentsTurn() {
            Permanent perm = createPermanent("Geralf, the Fleshwright");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = SpellCastTriggerEffect.atLeastDuringYourTurn(2, null, List.of(innerEffect));
            Card firstSpell = createInstant("Opt");
            Card secondSpell = createInstant("Consider");
            gd.activePlayerId = player2Id;
            gd.recordSpellCast(player1Id, firstSpell);
            gd.recordSpellCast(player1Id, secondSpell);

            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    new TriggerContext.SpellCast(secondSpell, player1Id, true), true)).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("queues targeting triggered ability as a SpellTargetTriggerAnyTarget interaction")
        void putsTargetingTriggeredAbilityIntoPendingQueue() {
            Permanent perm = createPermanent("Guttersnipe");
            var innerEffect = new DealDamageToAnyTargetEffect(2);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("queues a modal triggered ability for mode selection")
        void queuesModalTriggeredAbility() {
            Permanent perm = createPermanent("Kykar, Zephyr Awakener");
            var modal = new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("First mode", new PutCountersOnSourceEffect(0, 0, 1)),
                    new ChooseOneEffect.ChooseOneOption("Second mode", new CreateTokenEffect("Spirit", 1, 1,
                            CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()))
            ));
            var effect = new SpellCastTriggerEffect(null, List.of(modal));
            Card spellCard = createInstant("Opt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions)
                    .filteredOn(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                    .hasSize(1);
            var pending = gd.pendingInteractions.stream()
                    .filter(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                    .map(PermanentChoiceContext.TriggeredModalTrigger.class::cast)
                    .findFirst()
                    .orElseThrow();
            assertThat(pending.triggeringCardId()).isEqualTo(spellCard.getId());
        }

        @Test
        @DisplayName("queues a turn-scoped modal spell-cast trigger")
        void queuesTurnScopedModalSpellCastTrigger() {
            Permanent perm = createPermanent("The Vision");
            var filter = new CardNamedPredicate("Opt");
            var effect = new ChooseModeNotYetChosenThisTurnOnSpellCastEffect(filter, List.of(
                    new ChooseOneEffect.ChooseOneOption("First mode", new PutCountersOnSourceEffect(0, 0, 1)),
                    new ChooseOneEffect.ChooseOneOption("Second mode", new CreateTokenEffect("Spirit", 1, 1,
                            CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()))));
            Card spellCard = createInstant("Opt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(perm.getOriginalCard().getId()), any(), eq(player1Id))).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            var pending = gd.pendingInteractions.stream()
                    .filter(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                    .map(PermanentChoiceContext.TriggeredModalTrigger.class::cast)
                    .findFirst()
                    .orElseThrow();
            assertThat(pending.modesResetEachTurn()).isTrue();
            assertThat(pending.triggeringCardId()).isEqualTo(spellCard.getId());
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Guttersnipe");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var innerEffect = new DealDamageToAnyTargetEffect(2);
            var effect = new SpellCastTriggerEffect(filter, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(false);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), authored(effect),
                    ctx, true);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Whenever [a player] copies a spell — collectCopyAbility")
    class SpellCopyAbility {

        private boolean collectCopy(Permanent perm, SpellFilter filter, StackEntry copiedSpell, UUID copyingPlayerId,
                                    CardEffect... effects) {
            return sut.collectCopyAbility(match(perm, player1Id, effects[0]), filter, List.of(effects),
                    new TriggerContext.SpellCopy(copiedSpell, copyingPlayerId));
        }

        private StackEntry copyOf(Card spellCard, UUID controllerId) {
            StackEntry copiedSpell = new StackEntry(spellCard, controllerId);
            copiedSpell.setCopy(true);
            return copiedSpell;
        }

        @Test
        @DisplayName("puts triggered ability on stack for a matching copied spell")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Clever Lumimancer");

            boolean result = collectCopy(perm, SpellFilter.ANY, copyOf(createInstant("Lightning Bolt"), player1Id),
                    player1Id, new BoostSelfEffect(2, 2));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player1Id);
            assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("filters copied spells by the source Aura's enchanted player")
        void filtersCopiedSpellsByEnchantedPlayer() {
            Permanent perm = createPermanent("Curse of Shaken Faith");
            perm.setAttachedTo(player2Id);
            StackEntry copiedSpell = copyOf(createInstant("Lightning Bolt"), player1Id);
            when(predicateEvaluationService.matchesStackEntryPredicate(
                    eq(copiedSpell), any(), eq(player2Id))).thenReturn(false);

            boolean result = collectCopy(perm,
                    SpellFilter.ANY.withStackEntry(new StackEntryControlledByEnchantedPlayerPredicate()),
                    copiedSpell, player1Id, new BoostSelfEffect(2, 2));

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("a copied permanent spell triggers it too")
        void copiedPermanentSpellTriggersIt() {
            Permanent perm = createPermanent("Curse of Shaken Faith");
            perm.setAttachedTo(player1Id);
            StackEntry copiedSpell = copyOf(createCard("Grizzly Bears"), player1Id);
            when(predicateEvaluationService.matchesStackEntryPredicate(
                    eq(copiedSpell), any(), eq(player1Id))).thenReturn(true);

            boolean result = collectCopy(perm,
                    SpellFilter.ANY.withStackEntry(new StackEntryControlledByEnchantedPlayerPredicate()),
                    copiedSpell, player1Id, new BoostSelfEffect(2, 2));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("queues target selection from the target group a targeted copy ability's effect is bound to")
        void queuesTargetSelectionForTargetedCopiedSpell() {
            Card source = createCard("Symmetry Sage");
            var setPower = SetBasePowerToughnessEffect.powerOnly(2);
            source.target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youCopySpell().anySpell(), setPower);
            Permanent perm = new Permanent(source);

            boolean result = collectCopy(perm, SpellFilter.ANY, copyOf(createInstant("Lightning Bolt"), player1Id),
                    player1Id, setPower);

            assertThat(result).isTrue();
            assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class))
                    .satisfies(queued -> assertThat(queued.targetFilter())
                            .isSameAs(source.getTargetGroups().getFirst().getFilter()));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("queues graveyard target selection for a copied spell trigger")
        void queuesGraveyardTargetSelectionForCopiedSpell() {
            Permanent perm = createPermanent("Extus, Oriq Overlord");
            var returnCreature = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .targetGraveyard(true)
                    .build();

            boolean result = collectCopy(perm, SpellFilter.ANY, copyOf(createInstant("Lightning Bolt"), player1Id),
                    player1Id, returnCreature);

            assertThat(result).isTrue();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)).isTrue();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("puts triggered ability on stack for a matching copied spell controlled by an opponent")
        void putsTriggeredAbilityOnStackForOpponentCopy() {
            Permanent perm = createPermanent("Mage Hunter");

            boolean result = collectCopy(perm, SpellFilter.ANY, copyOf(createInstant("Lightning Bolt"), player2Id),
                    player2Id, new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2Id);
            assertThat(gd.stack.getLast().isNonTargeting()).isTrue();
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — BoostEquippedCreatureUntilEndOfTurnEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — BoostEquippedCreatureUntilEndOfTurnEffect")
    class ControllerBoostEquippedOnSpellCast {

        @Test
        @DisplayName("puts triggered ability on stack carrying the source permanent id")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Leering Emblem");
            var effect = new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("A cast ability's entry — the triggering spell")
    class TriggeringSpell {

        @Test
        @DisplayName("carries a copy of the spell as it is on the stack, its effect put on the stack unchanged")
        void entryCarriesTheTriggeringSpell() {
            Permanent perm = createPermanent("Izzet Steam Maze");
            var copy = new CopyTriggeringSpellEffect();
            var effect = castAbility(Triggers.playerCastsSpell().anySpell(), copy);
            Card spellCard = createInstant("Lightning Bolt");
            StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, spellCard, player2Id,
                    spellCard.getName(), List.of(), 0, player1Id, java.util.Map.of());
            gd.stack.add(spell);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, copy), effect, ctx, true);

            assertThat(result).isTrue();
            StackEntry entry = gd.stack.getLast();
            assertThat(entry.getEffectsToResolve()).containsExactly(copy);
            assertThat(entry.getTriggeringCardId()).isEqualTo(spellCard.getId());
            assertThat(entry.getTriggeringSpellSnapshot()).isNotSameAs(spell);
            assertThat(entry.getTriggeringSpellSnapshot().getCard()).isSameAs(spellCard);
            assertThat(entry.getTriggeringSpellSnapshot().getControllerId()).isEqualTo(player2Id);
            assertThat(entry.getTriggeringSpellSnapshot().getTargetId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("chooses a graveyard target restricted by the spell's mana value with that mana value as X")
        void graveyardTargetChoiceHasTheSpellsManaValueAsX() {
            Permanent perm = createPermanent("Krile Baldesion");
            ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .filter(new CardTypePredicate(CardType.CREATURE))
                    .targetGraveyard(true)
                    .requiresManaValueEqualsX(true)
                    .build();
            var effect = castAbility(Triggers.youCastSpell().anySpell(), returnEffect);
            Card spellCard = createInstant("Divination");
            spellCard.setManaCost("{2}{U}");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, returnEffect), effect, ctx, true);

            assertThat(result).isTrue();
            var choice = gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
            assertThat(choice).isNotNull();
            assertThat(choice.xValue()).isEqualTo(3);
            assertThat(choice.effects()).containsExactly(returnEffect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — same-name artifact search")
    class ControllerSameNameArtifactSearch {

        @Test
        @DisplayName("puts the ability on the stack with its may unchanged and the spell as its triggering card (CR 603.5)")
        void searchReadsTheTriggeringSpellAsItResolves() {
            Permanent perm = createPermanent("Mishra, Artificer Prodigy");
            var may = new MayEffect(SearchZonesForCardNamedToBattlefieldEffect.namedAsTriggeringSpell(),
                    "Search for a copy?");
            var ability = castAbility(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                    may);
            Card spellCard = createCard("Spellbook");
            spellCard.setType(CardType.ARTIFACT);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, may), ability, ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getTriggeringCardId()).isEqualTo(spellCard.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(may);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — a spell cast from your graveyard")
    class ControllerCastFromGraveyard {

        @Test
        @DisplayName("returns false when spell was cast from hand")
        void returnsFalseWhenCastFromHand() {
            Permanent perm = createPermanent("Snapcaster Mage");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = castAbility(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), innerEffect);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, ctx, true);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("puts triggered ability on stack when cast from graveyard with non-targeting effects")
        void putsOnStackWhenNoTargetingNeeded() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = castAbility(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), innerEffect);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(innerEffect);
        }

        @Test
        @DisplayName("adds to pending target triggers when effect needs targeting")
        void addsToPendingTargetTriggersWhenTargeting() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new DealDamageToAnyTargetEffect(3);
            var effect = castAbility(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), innerEffect);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("broadcasts log message when targeting is needed")
        void broadcastsLogWhenTargeting() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new DealDamageToAnyTargetEffect(3);
            var effect = castAbility(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD), innerEffect);
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);
            everySpellMatches();

            sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()), effect, ctx, true);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — DealDamageEqualToSpellManaValueToAnyTargetEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — DealDamageEqualToSpellManaValueToAnyTargetEffect")
    class ControllerManaValueDamage {

        @Test
        @DisplayName("adds to pending target triggers when spell matches filter")
        void addsToPendingTargetTriggers() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("resolved effect has damage equal to spell's mana value")
        void resolvedEffectHasCorrectDamage() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            var resolved = (DealDamageToAnyTargetEffect) gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class).effects().getFirst();
            assertThat(resolved.damage()).isEqualTo(new Fixed(2));
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)).isFalse();
        }

        @Test
        @DisplayName("broadcasts log message with damage amount")
        void broadcastsLogMessage() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new DealDamageEqualToSpellManaValueToAnyTargetEffect(filter);
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Kaervek the Merciless's triggered ability triggers — choose a target for 2 damage.")));
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — a target restricted by the spell's mana value")
    class TargetRestrictedBySpellManaValue {

        @Test
        @DisplayName("queues the target choice with the triggering spell's full mana value as its X")
        void queuesTheSpellsManaValueAsX() {
            Permanent perm = createPermanent("Skyfire Kirin");
            Card spellCard = createCard("Test Spell");
            spellCard.setManaCost("{X}{1}{R}");
            gd.stack.add(new StackEntry(
                    StackEntryType.CREATURE_SPELL, spellCard, player1Id, spellCard.getName(), List.of(), 2));
            everySpellMatches();
            var targetFilter = new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(), new PermanentManaValueEqualsXPredicate())),
                    "Target must be a creature with that spell's mana value");
            var gainControl = new GainControlOfTargetEffect(ControlDuration.END_OF_TURN);
            var ability = new CastAbility(SpellFilter.ANY, List.of(new MayEffect(gainControl, "Gain control?")), null,
                    targetFilter);

            boolean result = sut.collectCastAbility(match(perm, player1Id, gainControl), ability,
                    new TriggerContext.SpellCast(spellCard, player1Id, true), true);

            assertThat(result).isTrue();
            var queued = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(queued.targetFilter()).isEqualTo(targetFilter);
            assertThat(queued.spellManaSpentX()).isEqualTo(4);
            assertThat(queued.effects()).containsExactly(new MayEffect(gainControl, "Gain control?"));
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — GivePoisonCountersEffect (TARGET_PLAYER) =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — GivePoisonCountersEffect (TARGET_PLAYER)")
    class ControllerPoisonOnSpellCast {

        @Test
        @DisplayName("adds to pending target triggers when spell matches filter")
        void addsToPendingTargetTriggers() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER, filter);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
            assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class).playerTargetOnly()).isTrue();
        }

        @Test
        @DisplayName("returns false when spell filter is null")
        void returnsFalseWhenFilterNull() {
            Permanent perm = createPermanent("Hand of the Praetors");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when spell does not match filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER, filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("broadcasts log message")
        void broadcastsLogMessage() {
            Permanent perm = createPermanent("Hand of the Praetors");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER, filter);
            Card spellCard = createCard("Plague Stinger");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessDiscardEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessDiscardEffect")
    class OpponentLoseLifeUnlessDiscard {

        @Test
        @DisplayName("puts triggered ability on stack with casting player as target")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Bloodchief Ascension");
            var effect = new LoseLifeUnlessDiscardEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Bloodchief Ascension");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("stack entry includes the LoseLifeUnlessDiscardEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Bloodchief Ascension");
            var effect = new LoseLifeUnlessDiscardEffect(3);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — CounterUnlessPaysEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — CounterUnlessPaysEffect")
    class OpponentCounterUnlessPays {

        @Test
        @DisplayName("puts triggered ability on stack targeting the spell on the stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Chalice of the Void");
            var effect = new CounterUnlessPaysEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Chalice of the Void");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(stackEntry.getTargetZone()).isEqualTo(Zone.STACK);
        }

        @Test
        @DisplayName("stack entry includes the CounterUnlessPaysEffect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Chalice of the Void");
            var effect = new CounterUnlessPaysEffect(2);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — RevealTopCardCreatureToBattlefieldOrMayBottomEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — RevealTopCardCreatureToBattlefieldOrMayBottomEffect")
    class OpponentRevealTopCard {

        @Test
        @DisplayName("puts triggered ability on stack")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Lurking Predators");
            var effect = new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Lurking Predators");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("stack entry includes the effect")
        void stackEntryIncludesEffect() {
            Permanent perm = createPermanent("Lurking Predators");
            var effect = new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessPaysEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — LoseLifeUnlessPaysEffect")
    class OpponentLoseLifeUnlessPays {

        @Test
        @DisplayName("puts triggered ability on stack with casting player as target")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            var effect = new LoseLifeUnlessPaysEffect(2, 1);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Kambal, Consul of Allocation");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns false when spell does not match spell filter")
        void returnsFalseWhenFilterDoesNotMatch() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            CardPredicate filter = new CardNamedPredicate("Test Filter");
            var effect = new LoseLifeUnlessPaysEffect(2, 1, filter);
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(null), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("triggers when spell filter is null (no filter)")
        void triggersWhenNoFilter() {
            Permanent perm = createPermanent("Kambal, Consul of Allocation");
            var effect = new LoseLifeUnlessPaysEffect(2, 1);
            Card spellCard = createCard("Any Spell");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }
    }

    // ===== ON_OPPONENT_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect")
    class OpponentColorCounter {

        @Test
        @DisplayName("triggers when opponent's spell color matches")
        void triggersOnMatchingColor() {
            Permanent perm = createPermanent("Some Permanent");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.RED), 1, false);
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("returns false when opponent's spell color does not match")
        void returnsFalseWhenColorDoesNotMatch() {
            Permanent perm = createPermanent("Some Permanent");
            var effect = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.RED), 1, false);
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — CopySpellForEachPriorInstantOrSorceryEffect")
    class ControllerCopySpellForEachPriorInstantOrSorcery {

        @Test
        @DisplayName("counts only prior matching spells cast by the controller")
        void countsOnlyPriorControllerInstantsAndSorceries() {
            Permanent perm = createPermanent("Thousand-Year Storm");
            var effect = new CopySpellForEachPriorInstantOrSorceryEffect();
            Card priorInstant = createInstant("Prior Instant");
            Card currentSpell = createInstant("Current Spell");
            gd.recordSpellCast(player1Id, priorInstant);
            gd.recordSpellCast(player1Id, createCard("Creature Spell"));
            gd.recordSpellCast(player2Id, createInstant("Opponent Instant"));
            gd.recordSpellCast(player1Id, currentSpell);
            gd.stack.add(new StackEntry(currentSpell, player1Id));
            var ctx = new TriggerContext.SpellCast(currentSpell, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            var copyEffect = (StormCopyEffect) gd.stack.getLast().getEffectsToResolve().getFirst();
            assertThat(copyEffect.copies()).isEqualTo(1);
            assertThat(copyEffect.spellSnapshot().getCard()).isSameAs(currentSpell);
        }

        @Test
        @DisplayName("does not trigger for a creature spell")
        void ignoresCreatureSpell() {
            Permanent perm = createPermanent("Thousand-Year Storm");
            var effect = new CopySpellForEachPriorInstantOrSorceryEffect();
            Card creatureSpell = createCard("Creature Spell");
            gd.stack.add(new StackEntry(creatureSpell, player1Id));
            var ctx = new TriggerContext.SpellCast(creatureSpell, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — a player's first multicolored spell each turn")
    class FirstMulticoloredSpellCastTrigger {

        @Test
        @DisplayName("tracks the first multicolored spell independently for each player")
        void tracksEachPlayerIndependently() {
            Permanent perm = createPermanent("Zenith Chronicler");
            var effect = castAbility(Triggers.playerCastsSpell().withFilter(new CardIsMulticoloredPredicate()).nthThisTurn(1),
                    new BoostSelfEffect(1, 1));
            Card player1Spell = createCard("Player 1 Spell");
            player1Spell.setColors(List.of(CardColor.RED, CardColor.BLUE));
            Card player2Spell = createCard("Player 2 Spell");
            player2Spell.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
            everySpellMatches();

            gd.recordSpellCast(player1Id, player1Spell);
            boolean player1Result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(player1Spell, player1Id, true), true);

            gd.recordSpellCast(player2Id, player2Spell);
            boolean player2Result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(player2Spell, player2Id, true), true);

            assertThat(player1Result).isTrue();
            assertThat(player2Result).isTrue();
            assertThat(gd.stack).hasSize(2);
        }

        @Test
        @DisplayName("does not trigger for a player's later multicolored spell")
        void ignoresLaterMulticoloredSpell() {
            Permanent perm = createPermanent("Zenith Chronicler");
            var effect = castAbility(Triggers.playerCastsSpell().withFilter(new CardIsMulticoloredPredicate()).nthThisTurn(1),
                    new BoostSelfEffect(1, 1));
            Card firstSpell = createCard("First Spell");
            firstSpell.setColors(List.of(CardColor.RED, CardColor.BLUE));
            Card secondSpell = createCard("Second Spell");
            secondSpell.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
            everySpellMatches();

            gd.recordSpellCast(player1Id, firstSpell);
            sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(firstSpell, player1Id, true), true);
            gd.recordSpellCast(player1Id, secondSpell);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(secondSpell, player1Id, true), true);

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — the first noncreature spell of a turn")
    class FirstNoncreatureSpellCastTrigger {

        @Test
        @DisplayName("tracks the first noncreature spell globally across all players")
        void tracksFirstNoncreatureSpellGlobally() {
            Permanent perm = createPermanent("Nullstone Gargoyle");
            var effect = castAbility(Triggers.playerCastsSpell()
                            .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                            .nthThisTurn(1)
                            .countingEveryPlayersSpells(),
                    new CounterSpellEffect());
            Card creatureSpell = createCard("Creature Spell");
            Card firstNoncreatureSpell = createInstant("First Noncreature Spell");
            Card laterNoncreatureSpell = createInstant("Later Noncreature Spell");
            when(predicateEvaluationService.matchesCardPredicate(
                    any(Card.class), any(), any(), eq(gd), any(UUID.class), isNull(), isNull(), any()))
                    .thenAnswer(call -> !call.<Card>getArgument(0).hasType(CardType.CREATURE));
            when(predicateEvaluationService.matchesCardPredicate(any(Card.class), any(), isNull(), eq(gd),
                    any(UUID.class)))
                    .thenAnswer(call -> !call.<Card>getArgument(0).hasType(CardType.CREATURE));

            gd.recordSpellCast(player1Id, creatureSpell);
            boolean creatureResult = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(creatureSpell, player1Id, true), true);

            gd.recordSpellCast(player2Id, firstNoncreatureSpell);
            boolean firstResult = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(firstNoncreatureSpell, player2Id, true), true);

            gd.recordSpellCast(player1Id, laterNoncreatureSpell);
            boolean laterResult = sut.collectCastAbility(match(perm, player1Id, effect.effects().getFirst()),
                    effect, new TriggerContext.SpellCast(laterNoncreatureSpell, player1Id, true), true);

            assertThat(creatureResult).isFalse();
            assertThat(firstResult).isTrue();
            assertThat(laterResult).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }
}
