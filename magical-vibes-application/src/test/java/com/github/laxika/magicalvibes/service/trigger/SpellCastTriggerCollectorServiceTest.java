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
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachPriorInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
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
import static org.mockito.ArgumentMatchers.eq;
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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getDescription()).contains("Shrine of Burning Rage");
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    eq(perm.getOriginalCard().getId()), any(), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    eq(aura.getOriginalCard().getId()), any(), any())).thenReturn(true);

            registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);
            when(amountEvaluationService.referencesXValue(new XValue())).thenReturn(true);
            gd.addSpellCastManaSpent(spellCard.getId(), 3);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getXValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("adds to pending may abilities when wrapped in MayEffect")
        void addsToMayAbilitiesWhenMayEffect() {
            Permanent perm = createPermanent("Angel's Feather");
            var innerEffect = new PutCountersOnSourceEffect(0, 0, 1);
            var spellCastTrigger = new SpellCastTriggerEffect(null, List.of(innerEffect));
            var mayEffect = new MayEffect(spellCastTrigger, "Gain 1 life?");
            Card spellCard = createCard("Lightning Bolt", CardColor.RED);
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, mayEffect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Angel's Feather");
        }

        @Test
        @DisplayName("targeting trigger with a manaCost queues the target choice with the effect wrapped in MayPayManaEffect")
        void targetingTriggerWithManaCostWrapsInMayPayMana() {
            Permanent perm = createPermanent("Malachite Talisman");
            var innerEffect = new UntapPermanentsEffect(TapUntapScope.TARGET);
            var effect = new SpellCastTriggerEffect(null, List.of(innerEffect), "{3}", TargetFilters.permanent());
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
            var queued = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) gd.pendingInteractions.getFirst();
            assertThat(queued.controllerId()).isEqualTo(player1Id);
            assertThat(queued.effects()).singleElement()
                    .isEqualTo(new MayPayManaEffect("{3}", innerEffect, "Pay {3}?"));
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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(2);
            StackEntry triggerEntry = gd.stack.getLast();
            assertThat(triggerEntry.getTargetId()).isEqualTo(spellCard.getId());
            assertThat(triggerEntry.getTargetZone()).isEqualTo(Zone.STACK);
            assertThat(triggerEntry.getEffectsToResolve()).singleElement()
                    .isEqualTo(new CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType.CHARGE, 2));
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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds to pending may abilities when wrapped in MayEffect")
        void addsToMayAbilitiesWhenMayEffect() {
            Permanent perm = createPermanent("Wurm's Tooth");
            var inner = new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                    Set.of(CardColor.GREEN), 1, false);
            var mayEffect = new MayEffect(inner, "Gain 1 life?");
            Card spellCard = createCard("Grizzly Bears", CardColor.GREEN);
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, mayEffect),
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, mayEffect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().sourcePermanentId()).isEqualTo(perm.getId());
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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("does not trigger when the source intervening condition is false")
        void doesNotTriggerWhenInterveningConditionFails() {
            Permanent perm = createPermanent("Opal Acrolith");
            var effect = SpellCastTriggerEffect.withIntervening(null,
                    List.of(new PutCountersOnSourceEffect(1, 1, 1)), new SourceIsEnchantment());
            Card spellCard = createCard("Grizzly Bears");
            var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

            when(predicateEvaluationService.matchesCardPredicate(eq(spellCard), eq(null),
                    eq(perm.getOriginalCard().getId()), any(), any()))
                    .thenReturn(true);
            when(conditionEvaluationService.isMet(eq(gd), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
                EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
    @DisplayName("A same-name graveyard damage trigger binds the caster as a non-target")
    void sameNameGraveyardDamageTriggerBindsCaster() {
        Permanent perm = createPermanent("Dwarven Shrine");
        var effect = new DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

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
    @DisplayName("A same-name graveyard counter trigger snapshots the spell name and targets the spell")
    void sameNameGraveyardCounterTriggerSnapshotsSpellName() {
        Permanent perm = createPermanent("Cephalid Shrine");
        var effect = new CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect();
        Card spellCard = createInstant("Grizzly Bears");
        var ctx = new TriggerContext.SpellCast(spellCard, player2Id, true);

        boolean result = registry.dispatch(match(perm, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(spellCard.getId());
        assertThat(gd.stack.getLast().getTargetZone()).isEqualTo(Zone.STACK);
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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    any(Card.class), eq(filter), any(), eq(gd), eq(player1Id)))
                    .thenAnswer(invocation -> invocation.getArgument(0, Card.class).hasType(CardType.CREATURE));

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    eq(perm.getOriginalCard().getId()), any(), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions)
                    .filteredOn(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                    .hasSize(1);
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
                    eq(perm.getOriginalCard().getId()), any(), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getLast();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    // ===== ON_CONTROLLER_CASTS_SPELL — CastFromGraveyardTriggerEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_CASTS_SPELL — CastFromGraveyardTriggerEffect")
    class ControllerCastFromGraveyard {

        @Test
        @DisplayName("returns false when spell was cast from hand")
        void returnsFalseWhenCastFromHand() {
            Permanent perm = createPermanent("Snapcaster Mage");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, true);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("puts triggered ability on stack when cast from graveyard with non-targeting effects")
        void putsOnStackWhenNoTargetingNeeded() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new BoostSelfEffect(1, 1);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            boolean result = registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("broadcasts log message when targeting is needed")
        void broadcastsLogWhenTargeting() {
            Permanent perm = createPermanent("Some Card");
            var innerEffect = new DealDamageToAnyTargetEffect(3);
            var effect = new CastFromGraveyardTriggerEffect(List.of(innerEffect));
            Card spellCard = createInstant("Lightning Bolt");
            var ctx = new TriggerContext.SpellCast(spellCard, player1Id, false);

            registry.dispatch(
                    match(perm, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Kaervek the Merciless's triggered ability triggers — choose a target for 2 damage.")));
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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

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
                    EffectSlot.ON_CONTROLLER_CASTS_SPELL, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).hasSize(1);
        }
    }
}
