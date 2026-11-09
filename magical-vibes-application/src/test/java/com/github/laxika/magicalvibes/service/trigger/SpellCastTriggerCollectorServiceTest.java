package com.github.laxika.magicalvibes.service.trigger;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.GameLogEntry;

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
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.SpellsCastBeforeTriggeringSpellThisTurn;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchZonesForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerOtherThanTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    /** A cast ability of {@code filter} and {@code effects}, with no intervening "if" or target filter of its own. */
    private static CastAbility authored(SpellFilter filter, List<CardEffect> effects) {
        return new CastAbility(filter, effects, null, null);
    }

    /** The match of {@code ability}'s source, its first effect the matched effect. */
    private TriggerMatchContext match(Permanent perm, UUID controllerId, CastAbility ability) {
        return match(perm, controllerId, ability.effects().getFirst());
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
    @DisplayName("a first-spell-each-turn cast trigger choosing another target player")
    class FirstSpellCastTrigger {

        /** The Lord of Pain's ability: "their first spell each turn", "choose another target player". */
        private CastAbility lordOfPainAbility() {
            SpellTrigger trigger = Triggers.playerCastsSpell().nthThisTurn(1);
            return new CastAbility(trigger.filter(),
                    List.of(new DealDamageToPlayersEffect(new TargetSpellManaValue(), DamageRecipient.TARGET_PLAYER)),
                    null, new PlayerPredicateTargetFilter(new PlayerOtherThanTriggeringPlayerPredicate(),
                            "Target must be another player"));
        }

        @Test
        @DisplayName("tracks the first spell independently for each player, its target choice knowing the caster")
        void tracksEachPlayerIndependentlyAndCarriesTheCaster() {
            Permanent perm = createPermanent("The Lord of Pain");
            CastAbility ability = lordOfPainAbility();
            Card player1Spell = createCard("Player 1 Spell");
            Card player2Spell = createCard("Player 2 Spell");
            everySpellMatches();

            gd.recordSpellCast(player1Id, player1Spell);
            boolean player1Result = sut.collectCastAbility(match(perm, player1Id, ability), ability,
                    new TriggerContext.SpellCast(player1Spell, player1Id, true), false);
            gd.recordSpellCast(player2Id, player2Spell);
            boolean player2Result = sut.collectCastAbility(match(perm, player1Id, ability), ability,
                    new TriggerContext.SpellCast(player2Spell, player2Id, true), false);

            assertThat(player1Result).isTrue();
            assertThat(player2Result).isTrue();
            var pending = gd.pendingInteractions.stream()
                    .filter(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .map(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::cast)
                    .toList();
            assertThat(pending).extracting(PermanentChoiceContext.SpellTargetTriggerAnyTarget::triggeringPlayerId)
                    .containsExactly(player1Id, player2Id);
            assertThat(pending).allSatisfy(queued -> assertThat(queued.targetFilter())
                    .isEqualTo(ability.targetFilter()));
        }

        @Test
        @DisplayName("does not trigger for a player's later spell")
        void ignoresLaterSpell() {
            Permanent perm = createPermanent("The Lord of Pain");
            CastAbility ability = lordOfPainAbility();
            Card firstSpell = createCard("First Spell");
            Card secondSpell = createCard("Second Spell");
            everySpellMatches();

            gd.recordSpellCast(player1Id, firstSpell);
            sut.collectCastAbility(match(perm, player1Id, ability), ability,
                    new TriggerContext.SpellCast(firstSpell, player1Id, true), false);
            gd.recordSpellCast(player1Id, secondSpell);

            boolean result = sut.collectCastAbility(match(perm, player1Id, ability), ability,
                    new TriggerContext.SpellCast(secondSpell, player1Id, true), false);

            assertThat(result).isFalse();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance)
                    .hasSize(1);
        }
    }

    @Test
    void referringToTheTriggeringSpellDoesNotMakeItADeclaredTarget() {
        Permanent source = createPermanent("Spell watcher");
        Card spell = createInstant("Triggering spell");
        var effect = authored(SpellFilter.ANY, List.of(new BoostSelfEffect(2, 2),
                new MayEffect(new com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect(),
                        "Choose new targets?")));
        var context = new TriggerContext.SpellCast(spell, player1Id, true);
        when(predicateEvaluationService.matchesCardPredicate(eq(spell), eq(null),
                eq(source.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

        sut.collectCastAbility(match(source, player1Id, effect), effect, context, true);

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
        var effect = authored(SpellFilter.ANY, List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(new XValue())));
        when(predicateEvaluationService.matchesCardPredicate(spell, null,
                source.getOriginalCard().getId(), gd, player1Id, null, null, 3)).thenReturn(true);

        sut.collectCastAbility(match(source, player1Id, effect), effect,
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

    @Nested
    @DisplayName("ON_ANY_PLAYER_CASTS_SPELL — a filtered cast trigger")
    class AnyPlayerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Shrine of Burning Rage");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY.withTurn(SpellFilter.Turn.NOT_CASTERS),
                    List.of(new BoostSelfEffect(1, 1)));
            Card spellCard = createInstant("Opt");
            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            gd.activePlayerId = player1Id;
            boolean activeCasterResult = sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    new TriggerContext.SpellCast(spellCard, player1Id, true), false);

            assertThat(activeCasterResult).isFalse();
            assertThat(gd.stack).isEmpty();

            boolean nonActiveCasterResult = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY.withSpell(filter), List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(false);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    ctx, false);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("binds a spell target to the spell that caused the trigger")
        void bindsSpellTargetToTriggeringSpell() {
            Permanent perm = createPermanent("Presence of the Master");
            var effect = authored(SpellFilter.ANY, List.of(new CounterSpellEffect()));
            Card spellCard = createCard("Angelic Chorus");
            StackEntry spellOnStack = new StackEntry(spellCard, player2Id);
            gd.stack.add(spellOnStack);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            sut.collectCastAbility(match(perm, player1Id, effect), effect, ctx, false);

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
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(aura.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            sut.collectCastAbility(match(aura, player1Id, effect), effect, ctx, false);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getTargetId()).isEqualTo(host.getId());
            assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(aura.getId());
        }

        @Test
        @DisplayName("an ability reading the mana spent keeps the spell, its mana spent recorded, as its triggering spell")
        void manaSpentAmountReadsTheTriggeringSpell() {
            Permanent perm = createPermanent("Aberrant Manawurm");
            var innerEffect = new BoostSelfEffect(new ManaSpentToCast(), new Fixed(0));
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setManaSpentToCast(3);
            gd.stack.add(spellOnStack);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            sut.collectCastAbility(match(perm, player1Id, effect), effect, ctx, false);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getLast().getXValue()).isZero();
            assertThat(gd.stack.getLast().getTriggeringSpellSnapshot().getManaSpentToCast()).isEqualTo(3);
        }

        @Test
        @DisplayName("snapshots triggering spell mana value for EventValue counter amounts")
        void snapshotsTriggeringSpellManaValueForEventValueCounterAmounts() {
            Permanent perm = createPermanent("Chrome Host Seedshark");
            var innerEffect = new CreateXTokenWithXCountersEffect(
                    CreateTokenEffect.whiteSpirit(1), new EventValue(), CounterType.PLUS_ONE_PLUS_ONE);
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createInstant("Divination");
            spellCard.setManaCost("{2}{U}");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);
            sut.collectCastAbility(match(perm, player1Id, effect), effect, ctx, true);

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
            var draw = new DrawCardEffect(new ManaSpentToCast());
            var mayEffect = new MayEffect(draw, "Draw cards?");
            Card spellCard = createInstant("Dark Ritual");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);
            StackEntry spellOnStack = new StackEntry(spellCard, player1Id);
            spellOnStack.setManaSpentToCast(3);
            gd.stack.add(spellOnStack);
            everySpellMatches();
            CastAbility ability = castAbility(Triggers.playerCastsSpell().anySpell(), mayEffect);

            boolean result = sut.collectCastAbility(match(perm, player1Id, mayEffect), ability, ctx, false);

            assertThat(result).isTrue();
            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack.getLast()).satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(new MayEffect(draw, "Draw cards?"));
                assertThat(entry.getTriggeringSpellSnapshot().getManaSpentToCast()).isEqualTo(3);
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

    @Test
    @DisplayName("A same-name graveyard trigger goes on the stack as written, the caster its non-targeting player")
    void sameNameGraveyardTriggerGoesOnTheStackAsWritten() {
        Permanent perm = createPermanent("Aven Shrine");
        CardEffect gainLife = new GainLifeEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER),
                GainLifeRecipient.TRIGGERING_PLAYER);
        CastAbility ability = castAbility(Triggers.playerCastsSpell().anySpell(), gainLife);
        Card spellCard = createInstant("Grizzly Bears");
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, spellCard, player2Id, "Grizzly Bears",
                new ArrayList<>()));
        everySpellMatches();

        boolean result = sut.collectCastAbility(match(perm, player1Id, gainLife), ability,
                new TriggerContext.SpellCast(spellCard, player2Id, true), false);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getTargetId()).isEqualTo(player2Id);
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTriggeringCardId()).isEqualTo(spellCard.getId());
        assertThat(entry.getTriggeringSpellSnapshot().getCard()).isEqualTo(spellCard);
        assertThat(entry.getEffectsToResolve()).containsExactly(gainLife);
    }

    @Test
    @DisplayName("A same-name graveyard return trigger goes on the stack as written, recording the triggering spell")
    void sameNameGraveyardReturnTriggerGoesOnTheStackAsWritten() {
        Permanent perm = createPermanent("Bloodbond March");
        CardEffect returnCards = new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                Integer.MAX_VALUE, new CardSharesNameWithTriggeringSpellPredicate());
        CastAbility ability = castAbility(
                Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)), returnCards);
        Card spellCard = createCard("Grizzly Bears");
        gd.stack.add(new StackEntry(StackEntryType.CREATURE_SPELL, spellCard, player2Id, "Grizzly Bears",
                new ArrayList<>()));
        everySpellMatches();

        boolean result = sut.collectCastAbility(match(perm, player1Id, returnCards), ability,
                new TriggerContext.SpellCast(spellCard, player2Id, true), false);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTriggeringCardId()).isEqualTo(spellCard.getId());
        assertThat(entry.getTriggeringSpellSnapshot().getCard()).isEqualTo(spellCard);
        assertThat(entry.getEffectsToResolve()).containsExactly(returnCards);
    }

    @Test
    @DisplayName("A filtered poison trigger asks for its target player as it triggers")
    void filteredPoisonTriggerQueuesItsPlayerTarget() {
        Permanent perm = createPermanent("Hand of the Praetors");
        CardEffect poison = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER);
        CastAbility ability = castAbility(
                Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)), poison);
        Card spellCard = createCard("Plague Stinger");
        gd.stack.add(new StackEntry(StackEntryType.CREATURE_SPELL, spellCard, player1Id, "Plague Stinger",
                new ArrayList<>()));
        everySpellMatches();

        boolean result = sut.collectCastAbility(match(perm, player1Id, poison), ability,
                new TriggerContext.SpellCast(spellCard, player1Id, true), false);

        assertThat(result).isTrue();
        PermanentChoiceContext.SpellTargetTriggerAnyTarget queued =
                gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(queued.playerTargetOnly()).isTrue();
        assertThat(queued.effects()).containsExactly(poison);
    }

    @Test
    @DisplayName("A same-name counter-unless trigger goes on the stack as written, aimed at the spell without targeting it")
    void sameNameCounterUnlessTriggerGoesOnTheStackAsWritten() {
        Permanent perm = createPermanent("Cephalid Shrine");
        CardEffect counter = new CounterUnlessPaysEffect(
                new CardsInGraveyard(new CardSharesNameWithTriggeringSpellPredicate(), CountScope.ANY_PLAYER));
        CastAbility ability = castAbility(Triggers.playerCastsSpell().anySpell(), counter);
        Card spellCard = createInstant("Grizzly Bears");
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, spellCard, player2Id, "Grizzly Bears",
                new ArrayList<>()));
        everySpellMatches();

        boolean result = sut.collectCastAbility(match(perm, player1Id, counter), ability,
                new TriggerContext.SpellCast(spellCard, player2Id, true), false);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getTargetId()).isEqualTo(spellCard.getId());
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTriggeringSpellSnapshot().getCard()).isEqualTo(spellCard);
        assertThat(entry.getEffectsToResolve()).containsExactly(counter);
    }

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — a filtered cast trigger")
    class ControllerSpellCastTrigger {

        @Test
        @DisplayName("puts triggered ability on stack when spell matches filter (non-targeting)")
        void putsTriggeredAbilityOnStack() {
            Permanent perm = createPermanent("Cabal Paladin");
            var innerEffect = new PutCountersOnSourceEffect(1, 1, 1);
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY.withSpell(filter).withNthThisTurn(2), List.of(innerEffect));
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

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    ctx, true);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("fires on the second and every later matching spell during the controller's turn")
        void firesOnSecondAndLaterMatchingSpells() {
            Permanent perm = createPermanent("Geralf, the Fleshwright");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = authored(SpellFilter.ANY.withNthOrLaterThisTurn(2).withTurn(SpellFilter.Turn.YOURS), List.of(innerEffect));
            Card firstSpell = createInstant("Opt");
            Card secondSpell = createInstant("Consider");
            Card thirdSpell = createInstant("Brainstorm");
            gd.activePlayerId = player1Id;

            when(predicateEvaluationService.matchesCardPredicate(any(Card.class), eq(null), any(),
                    eq(gd), eq(player1Id), isNull(), isNull(), any())).thenReturn(true);

            when(predicateEvaluationService.matchesCardPredicate(any(Card.class), isNull(), isNull(),
                    eq(gd), eq(player1Id))).thenReturn(true);

            gd.recordSpellCast(player1Id, firstSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    new TriggerContext.SpellCast(firstSpell, player1Id, true), true)).isFalse();

            gd.recordSpellCast(player1Id, secondSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    new TriggerContext.SpellCast(secondSpell, player1Id, true), true)).isTrue();

            gd.recordSpellCast(player1Id, thirdSpell);
            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    new TriggerContext.SpellCast(thirdSpell, player1Id, true), true)).isTrue();
            assertThat(gd.stack).hasSize(2);
        }

        @Test
        @DisplayName("does not fire the own-turn minimum spell trigger during an opponent's turn")
        void doesNotFireDuringOpponentsTurn() {
            Permanent perm = createPermanent("Geralf, the Fleshwright");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var effect = authored(SpellFilter.ANY.withNthOrLaterThisTurn(2).withTurn(SpellFilter.Turn.YOURS), List.of(innerEffect));
            Card firstSpell = createInstant("Opt");
            Card secondSpell = createInstant("Consider");
            gd.activePlayerId = player2Id;
            gd.recordSpellCast(player1Id, firstSpell);
            gd.recordSpellCast(player1Id, secondSpell);

            assertThat(sut.collectCastAbility(match(perm, player1Id, effect), effect,
                    new TriggerContext.SpellCast(secondSpell, player1Id, true), true)).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("queues targeting triggered ability as a SpellTargetTriggerAnyTarget interaction")
        void putsTargetingTriggeredAbilityIntoPendingQueue() {
            Permanent perm = createPermanent("Guttersnipe");
            var innerEffect = new DealDamageToAnyTargetEffect(2);
            var effect = authored(SpellFilter.ANY, List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY, List.of(modal));
            Card spellCard = createInstant("Opt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any()))
                    .thenReturn(true);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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
            var effect = authored(SpellFilter.ANY.withSpell(filter), List.of(innerEffect));
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(filter),
                    eq(perm.getOriginalCard().getId()), any(), any(), isNull(), isNull(), any())).thenReturn(false);

            boolean result = sut.collectCastAbility(match(perm, player1Id, effect), effect,
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

    @Nested
    @DisplayName("ON_OPPONENT_CASTS_SPELL — damage equal to the triggering spell's mana value")
    class ManaValueDamage {

        @Test
        @DisplayName("queues the effect as written, its target choice carrying the spell and its mana value")
        void queuesTheEffectAsWrittenWithTheSpellsManaValue() {
            Permanent perm = createPermanent("Kaervek the Merciless");
            Card spellCard = createCard("Grizzly Bears");
            spellCard.setManaCost("{1}{G}");
            gd.stack.add(new StackEntry(
                    StackEntryType.CREATURE_SPELL, spellCard, player2Id, spellCard.getName(), List.of(), 0));
            everySpellMatches();
            var damage = new DealDamageToAnyTargetEffect(new TargetSpellManaValue());

            boolean result = sut.collectCastAbility(match(perm, player1Id, damage),
                    castAbility(Triggers.opponentCastsSpell().anySpell(), damage),
                    new TriggerContext.SpellCast(spellCard, player2Id, true), true);

            assertThat(result).isTrue();
            var queued = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(queued.effects()).containsExactly(damage);
            assertThat(queued.triggeringSpellManaValue()).isEqualTo(2);
            assertThat(queued.triggeringCardId()).isEqualTo(spellCard.getId());
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

    @Nested
    @DisplayName("a cast trigger counting the spells cast before the triggering spell")
    class CastTriggerRecordsTriggeringSpellCastIndex {

        @Test
        @DisplayName("keeps the triggering spell's place among its caster's spells cast this turn")
        void keepsTheTriggeringSpellsPlaceAmongItsCastersSpells() {
            Permanent perm = createPermanent("Thousand-Year Storm");
            var ability = castAbility(Triggers.youCastSpell().anySpell(), new CopyTriggeringSpellEffect()
                    .withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(null)));
            Card currentSpell = createInstant("Current Spell");
            gd.recordSpellCast(player1Id, currentSpell);
            gd.recordSpellCast(player1Id, createInstant("Prior Instant"));
            gd.recordSpellCast(player2Id, createInstant("Opponent Instant"));
            gd.recordSpellCast(player1Id, currentSpell);
            gd.stack.add(new StackEntry(currentSpell, player1Id));
            everySpellMatches();

            boolean result = sut.collectCastAbility(match(perm, player1Id, ability), ability,
                    new TriggerContext.SpellCast(currentSpell, player1Id, true), false);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            // The card's latest casting, the one that triggered the ability.
            assertThat(gd.stack.getLast().getTriggeringSpellCastIndex()).isEqualTo(2);
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
    @DisplayName("a card-advantage ability and an intervening \"if\" on the triggering spell")
    class TriggeringSpellAbilities {

        @Test
        @DisplayName("a card-advantage ability goes on the stack as written, recording the spell that resolved")
        void cardAdvantageAbilityGoesOnTheStackAsWritten() {
            Permanent perm = createPermanent("Toofer, Keeper of the Full Grip");
            CardEffect loseLife = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
            TriggeredAbility ability = new TriggeredAbility(Triggers.yourSpellCausesYouToGainCardAdvantage(), loseLife);
            Card spellCard = createInstant("Divination");
            StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, spellCard, player1Id, "Divination",
                    new ArrayList<>());

            boolean result = sut.collectCardAdvantageAbility(match(perm, player1Id, loseLife), ability,
                    new TriggerContext.SpellCardAdvantage(spell));

            assertThat(result).isTrue();
            StackEntry entry = gd.stack.getLast();
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(spellCard.getId());
            assertThat(entry.getTriggeringSpellSnapshot().getCard()).isEqualTo(spellCard);
            assertThat(entry.getEffectsToResolve()).containsExactly(loseLife);
        }

        @Test
        @DisplayName("a card-advantage ability with an intervening \"if\" fails loudly")
        void cardAdvantageAbilityWithAnInterveningIfThrows() {
            Permanent perm = createPermanent("Toofer, Keeper of the Full Grip");
            CardEffect loseLife = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
            TriggeredAbility ability = new TriggeredAbility(Triggers.yourSpellCausesYouToGainCardAdvantage(),
                    TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), loseLife);
            StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, createInstant("Divination"), player1Id,
                    "Divination", new ArrayList<>());

            assertThatThrownBy(() -> sut.collectCardAdvantageAbility(match(perm, player1Id, loseLife), ability,
                    new TriggerContext.SpellCardAdvantage(spell)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("an intervening \"if\" on the triggering spell is judged on the spell as it triggers")
        void interveningIfOnTheTriggeringSpellIsJudgedAsItTriggers() {
            Permanent perm = createPermanent("Spellweaver Helix");
            CardEffect copy = new MayEffect(CopyImprintedCardAndMayCastCopyEffect.otherExiledCard(), "Copy it?");
            StackEntryPredicate sharesName = new StackEntrySharesNameWithCardExiledWithSourcePredicate();
            SpellTrigger trigger = Triggers.playerCastsSpell().anySpell();
            CastAbility ability = CastAbility.of(new TriggeredAbility(trigger,
                    TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(sharesName)), copy),
                    trigger.filter());
            Card matching = createInstant("Fabricate");
            Card other = createInstant("Annul");
            gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, matching, player2Id, "Fabricate",
                    new ArrayList<>()));
            gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, other, player2Id, "Annul", new ArrayList<>()));
            everySpellMatches();
            when(targetLegalityService.matchesStackEntryPredicate(eq(gd), any(StackEntry.class), eq(sharesName),
                    eq(player1Id), eq(perm)))
                    .thenAnswer(call -> call.<StackEntry>getArgument(1).getCard() == matching);

            boolean matched = sut.collectCastAbility(match(perm, player1Id, copy), ability,
                    new TriggerContext.SpellCast(matching, player2Id, true), false);
            boolean unmatched = sut.collectCastAbility(match(perm, player1Id, copy), ability,
                    new TriggerContext.SpellCast(other, player2Id, true), false);

            assertThat(matched).isTrue();
            assertThat(unmatched).isFalse();
            assertThat(gd.stack).hasSize(3);
            StackEntry entry = gd.stack.getLast();
            assertThat(entry.getTriggeringCardId()).isEqualTo(matching.getId());
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            assertThat(entry.getEffectsToResolve())
                    .containsExactly(new ConditionalEffect(new TriggeringSpellMatches(sharesName), copy));
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
