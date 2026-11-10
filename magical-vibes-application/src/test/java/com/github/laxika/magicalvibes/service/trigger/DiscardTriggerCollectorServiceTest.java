package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.SeekCardSharingCardTypeWithDiscardedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.service.GameLogService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscardTriggerCollectorServiceTest {

    @Mock
    private GameLogService gameLogService;

    @InjectMocks
    private DiscardTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    @Test
    @DisplayName("Queues a conditional discard trigger, its condition checked as it resolves")
    void queuesConditionalDiscardTriggerWhenConditionIsMet() {
        Permanent source = createPermanent("Spacecraft");
        var condition = new SourceCounterThreshold(1, CounterType.CHARGE);
        var wrapped = new DrawCardEffect(1);
        var effect = new ConditionalEffect(condition, wrapped);
        var ctx = new TriggerContext.Discard(player2Id, createCard("Discarded card"));

        boolean result = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
        });
    }

    @Test
    @DisplayName("Puts a turn-scoped modal discard ability on the stack as written, its mode announced there")
    void putsTurnScopedModalDiscardAbilityOnTheStackAsWritten() {
        Permanent source = createPermanent("Monument to Endurance");
        var effect = ChooseOneEffect.notYetChosenThisTurn(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Each opponent loses 3 life",
                        new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT))));
        var ctx = new TriggerContext.Discard(player1Id, createCard("Discarded card"));

        boolean result = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
        });
    }

    @Test
    @DisplayName("Queues a self-untap discard trigger with its source permanent")
    void queuesSelfUntapDiscardTrigger() {
        Permanent source = createPermanent("Anje Falkenrath");
        var effect = new UntapPermanentsEffect(TapUntapScope.SELF);
        var ctx = new TriggerContext.Discard(player1Id, createCard("Madness card"));

        boolean result = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    void cyclingQueuesKeywordGrantForItsSource() {
        Permanent source = createPermanent("Cycling observer");
        var effect = new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF);
        var context = new TriggerContext.Cycle(player2Id, createCard("Cycled card"));

        boolean collected = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_ANY_PLAYER_CYCLES, context);

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    @DisplayName("\"Whenever a player cycles a card\" has a default that records the cycling player and the cycled card")
    void cyclingDefaultRecordsTheCycle() {
        Permanent source = createPermanent("Cycling observer");
        var effect = new DrawCardEffect(1);
        Card cycled = createCard("Cycled card");
        var context = new TriggerContext.Cycle(player2Id, cycled);

        boolean collected = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_ANY_PLAYER_CYCLES, context);

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(cycled.getId());
            assertThat(entry.getTriggeringCards()).containsExactly(cycled);
        });
    }

    @Test
    @DisplayName("A targeted discard ability's target choice carries the discard its entry records")
    void targetedDiscardChoiceCarriesTheDiscard() {
        Permanent source = createPermanent("Ominous Sphinx");
        var effect = new BoostTargetCreatureEffect(-2, -2);
        Card discarded = createCard("Discarded card");
        var ctx = new TriggerContext.Discard(player1Id, discarded);

        boolean collected = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

        assertThat(collected).isTrue();
        assertThat(gd.stack).isEmpty();
        var choice = (PermanentChoiceContext.DiscardControllerTriggerTarget) gd.pendingInteractions.getFirst();
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.discardingPlayerId()).isEqualTo(player1Id);
        assertThat(choice.discardedCards()).containsExactly(discarded);
        assertThat(choice.discardedCardStays()).containsOnlyKeys(discarded.getId());

        StackEntry entry = DiscardTriggerCollectorService.recordDiscard(new StackEntry(
                        com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY, source.getCard(),
                        player1Id, "ability", List.of(effect)),
                choice.discardingPlayerId(), choice.discardedCards(), choice.discardedCardStays(), false);
        assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
        assertThat(entry.getTriggeringCardId()).isEqualTo(discarded.getId());
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Optional cycling triggers request targets before they can resolve")
    void optionalCyclingTriggerQueuesTargetChoiceBeforeResolution() {
        Permanent source = createPermanent("Cycling observer");
        var effect = new MayEffect(new BoostTargetCreatureEffect(1, 1), "Boost target creature?");
        var context = new TriggerContext.Cycle(player2Id, createCard("Cycled card"));

        boolean collected = registry.dispatch(match(source, player1Id, effect),
                LibraryWatch.ON_ANY_PLAYER_CYCLES, context);

        assertThat(collected).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("A discard-event seek trigger has the discarded cards as its triggering cards")
    void discardEventSeekHasTheDiscardedCardsAsItsTriggeringCards() {
        Permanent source = createPermanent("Diviner of Fates");
        var effect = new SeekCardSharingCardTypeWithDiscardedCardsEffect();
        Card first = createCard("First discarded card");
        Card second = createCard("Second discarded card");
        var ctx = new TriggerContext.DiscardEvent(player1Id, 2, List.of(first, second));

        boolean collected = registry.dispatch(
                match(source, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

        assertThat(collected).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTriggeringCards()).containsExactly(first, second);
        });
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — CastDiscardedCardFromGraveyardEffect")
    class ControllerDiscardCast {

        @Test
        @DisplayName("queues the exact discarded nonland card")
        void queuesExactDiscardedNonlandCard() {
            Permanent source = createPermanent("Oskar, Rubbish Reclaimer");
            var effect = new CastDiscardedCardFromGraveyardEffect();
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(source, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTriggeringCardId()).isEqualTo(discarded.getId());
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
            });
        }

    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — \"whenever you cycle a card\"")
    class ControllerCycling {

        @Test
        @DisplayName("puts the effect on the stack for a cycling the collector judged")
        void queuesTheEffect() {
            Permanent healer = createPermanent("Drannith Healer");
            var effect = new GainLifeEffect(1);

            boolean result = sut.collectCycling(match(healer, player1Id, effect),
                    new TriggerContext.Discard(player1Id, createCard("Cycled card"), true));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .containsExactly(new GainLifeEffect(1));
        }

        @Test
        @DisplayName("queues a target choice for a targeted cycling trigger")
        void queuesTargetChoiceForTargetedCyclingTrigger() {
            Permanent tactician = createPermanent("Snare Tactician");
            var effect = new TapPermanentsEffect(TapUntapScope.TARGET);

            boolean result = sut.collectCycling(match(tactician, player1Id, effect),
                    new TriggerContext.Discard(player1Id, createCard("Cycled card"), true));

            assertThat(result).isTrue();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class))
                    .isTrue();
            assertThat(gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)
                    .effects()).containsExactly(effect);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("a \"you discard a card\" effect no handler of its own collects goes on the stack through the default")
        void untargetedDiscardEffectGoesOnTheStackThroughTheDefault() {
            Permanent source = createPermanent("Discard Watcher");
            var effect = new GainLifeEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Censor"), true);

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry ->
                    assertThat(entry.getEffectsToResolve()).containsExactly(effect));
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — DealDamageToPlayersEffect")
    class DiscardDamage {

        @Test
        @DisplayName("queues a non-targeting damage trigger aimed at the discarding player")
        void queuesDamageTriggerForDiscardingPlayer() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));
            int lifeBefore = gd.getLife(player2Id);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(megrim.getId());
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }

    }

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS / ON_CONTROLLER_DISCARDS — LoseLifeEffect")
    class DiscardLifeLoss {

        @Test
        @DisplayName("queues a non-targeting life-loss trigger aimed at the discarding player")
        void queuesLifeLossTriggerForDiscardingPlayer() {
            Permanent enchantment = createPermanent("Liliana's Caress");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));
            int lifeBefore = gd.getLife(player2Id);

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentId()).isEqualTo(enchantment.getId());
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            });
        }

        @Test
        @DisplayName("queues the controller's own life-loss trigger on their discard")
        void queuesControllerLifeLossTrigger() {
            Permanent enchantment = createPermanent("Midnight Oil");
            var effect = new LoseLifeEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));
            int lifeBefore = gd.getLife(player1Id);

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player1Id)).isEqualTo(lifeBefore);
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getTargetId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            });
        }

    }

    // ===== ON_OPPONENT_DISCARDS — MayEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — MayEffect")
    class DiscardMay {

        @Test
        @DisplayName("queues may ability and returns true")
        void queuesMayAbility() {
            Permanent enchantment = createPermanent("Waste Not");
            var inner = new LoseLifeEffect(1);
            var effect = new MayEffect(inner, "Do you want to trigger?");
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

            assertThat(result).isTrue();
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_OPPONENT_DISCARDS — PutCountersOnSourceEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — PutCountersOnSourceEffect")
    class OpponentDiscardPutCounters {

        @Test
        @DisplayName("queues a source-counter trigger")
        void queuesSourceCounterTrigger() {
            Permanent source = createPermanent("Tourach, Dread Cantor");
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(source, player1Id, effect), LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            });
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — ExileDiscardedCardFromGraveyardEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — ExileDiscardedCardFromGraveyardEffect")
    class ControllerDiscardExile {

        @Test
        @DisplayName("queues the trigger with the discarded card and returns true")
        void queuesDiscardTrigger() {
            Permanent necro = createPermanent("Necropotence");
            var effect = new ExileDiscardedCardFromGraveyardEffect();
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(necro, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(necro.getId());
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(discarded.getId());
            assertThat(gd.playerGraveyards.get(player1Id)).contains(discarded);
        }

        @Test
        @DisplayName("tracks the discarded card with the source when requested")
        void tracksExiledCardWithSource() {
            Permanent bag = createPermanent("Bag of Holding");
            var effect = new ExileDiscardedCardFromGraveyardEffect(true);
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(bag, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(bag.getId());
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(discarded.getId());
        }

        @Test
        @DisplayName("still triggers when the discarded card is not in the graveyard, exiling nothing")
        void triggersEvenWhenNotInGraveyard() {
            Permanent necro = createPermanent("Necropotence");
            var effect = new ExileDiscardedCardFromGraveyardEffect();
            Card discarded = createCard("Grizzly Bears");
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(necro, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — ScryEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — ScryEffect")
    class ControllerDiscardScry {

        @Test
        @DisplayName("queues a scry triggered ability for the discarding player and returns true")
        void queuesScryTrigger() {
            Permanent curator = createPermanent("Curator of Mysteries");
            var effect = new ScryEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(curator, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(curator.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(ScryEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — MillEffect")
    class ControllerDiscardMill {

        @Test
        @DisplayName("queues a mill trigger for each opponent")
        void queuesMillTrigger() {
            Permanent source = createPermanent("Mystic Redaction");
            var effect = new MillEffect(2, MillRecipient.EACH_OPPONENT);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS - ExileTopCardMayPlayThisTurnEffect")
    class ControllerDiscardExileTopCard {

        @Test
        @DisplayName("queues an exile-and-play trigger for the controller")
        void queuesExileTopCardTrigger() {
            Permanent pyre = createPermanent("Pyre of the World Tree");
            var effect = new ExileTopCardMayPlayThisTurnEffect(false);
            Card discarded = createCard("Forest");
            discarded.setType(CardType.LAND);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(pyre, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(pyre.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — BoostSelfEffect")
    class ControllerDiscardSelfBoost {

        @Test
        @DisplayName("queues a self-boost triggered ability carrying the source permanent and returns true")
        void queuesSelfBoostTrigger() {
            Permanent hekma = createPermanent("Hekma Sentinels");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(hekma, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(hekma.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(BoostSelfEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — DealDamageToPlayersEffect")
    class ControllerDiscardDamageToEachOpponent {

        @Test
        @DisplayName("queues damage to each opponent with the source permanent and returns true")
        void queuesDamageTrigger() {
            Permanent buccaneer = createPermanent("Glint-Horn Buccaneer");
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(buccaneer, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(buccaneer.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }

        @Test
        @DisplayName("queues conditional damage, its condition checked as it resolves")
        void queuesConditionalDamageTrigger() {
            Permanent classPermanent = createPermanent("Cool but Rude");
            var effect = new ConditionalEffect(
                    new SourceCounterThreshold(1, CounterType.LEVEL),
                    new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(classPermanent, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — CreateTokenEffect")
    class ControllerDiscardCreateToken {

        @Test
        @DisplayName("queues a token-creation trigger for the controller")
        void queuesTokenCreationTrigger() {
            Permanent urza = createPermanent("Urza, Powerstone Prodigy");
            var effect = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Spellbook"));

            boolean result = registry.dispatch(
                    match(urza, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(urza.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — DealDamageToPlayersEffect")
    class ControllerDiscardEventDamageToEachOpponent {

        @Test
        @DisplayName("queues damage using the number of cards discarded in the event")
        void queuesDamageTriggerWithDiscardedCount() {
            Permanent artillerist = createPermanent("Magmakin Artillerist");
            var effect = new DealDamageToPlayersEffect(
                    new com.github.laxika.magicalvibes.model.amount.EventValue(), DamageRecipient.EACH_OPPONENT);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(artillerist, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(artillerist.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — LoseLifeEffect")
    class ControllerDiscardEventLifeLossToEachOpponent {

        @Test
        @DisplayName("queues life loss using the discard event")
        void queuesLifeLossTrigger() {
            Permanent doom = createPermanent("Doctor Doom, King of Latveria");
            var effect = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
            Card land = createCard("Forest");
            land.setType(CardType.LAND);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 2,
                    List.of(land, createCard("Grizzly Bears")));

            boolean result = registry.dispatch(
                    match(doom, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentId()).isEqualTo(doom.getId());
                assertThat(entry.getEventValue()).isEqualTo(2);
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            });
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — AwardManaEffect")
    class ControllerDiscardAwardMana {

        @Test
        @DisplayName("queues a mana trigger for the controller")
        void queuesManaTrigger() {
            Permanent mishra = createPermanent("Mishra, Excavation Prodigy");
            var effect = new AwardManaEffect(ManaColor.RED, 2);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Spellbook"));

            boolean result = registry.dispatch(
                    match(mishra, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(mishra.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — DrawCardEffect")
    class ControllerDiscardEventDraw {

        @Test
        @DisplayName("queues a draw using the number of cards discarded in the event")
        void queuesDrawTriggerWithDiscardedCount() {
            Permanent rielle = createPermanent("Rielle, the Everwise");
            var effect = new DrawCardEffect(new com.github.laxika.magicalvibes.model.amount.EventValue());
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(rielle, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(rielle.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — PutCountersOnSelfEffect")
    class ControllerDiscardEventSelfCounters {

        @Test
        @DisplayName("queues counters using the number of cards discarded in the event")
        void queuesCounterTriggerWithDiscardedCount() {
            Permanent mako = createPermanent("Marauding Mako");
            var effect = new PutCountersOnSelfEffect(
                    CounterType.PLUS_ONE_PLUS_ONE,
                    new com.github.laxika.magicalvibes.model.amount.EventValue());
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(mako, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(mako.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CYCLES — PutCountersOnSelfEffect")
    class CycleSelfCounters {

        @Test
        @DisplayName("queues a counter trigger with the source permanent")
        void queuesCounterTrigger() {
            Permanent aura = createPermanent("Withering Hex");
            var effect = new PutCountersOnSelfEffect(CounterType.PLAGUE);
            var ctx = new TriggerContext.Cycle(player1Id, createCard("Censor"));

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    LibraryWatch.ON_ANY_PLAYER_CYCLES, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — ExileTopCardsMayPlayUntilNextEndStepEffect")
    class ControllerDiscardEventExileTopCard {

        @Test
        @DisplayName("queues one top-card exile trigger for the discard event")
        void queuesTopCardExileTrigger() {
            Permanent inti = createPermanent("Inti, Seneschal of the Sun");
            var effect = new ExileTopCardsMayPlayUntilNextEndStepEffect(1);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(inti, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(inti.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — SequenceEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — SequenceEffect")
    class ControllerDiscardSequence {

        @Test
        @DisplayName("queues one atomic triggered ability carrying the source permanent and returns true")
        void queuesSequenceTrigger() {
            Permanent survivor = createPermanent("Cunning Survivor");
            var effect = SequenceEffect.of(new BoostSelfEffect(1, 0), new MakeCreatureUnblockableEffect(true));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(survivor, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(survivor.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(SequenceEffect.class);
        }

        @Test
        @DisplayName("queues target selection for a targeted sequence")
        void queuesTargetChoiceForTargetedSequence() {
            Permanent source = createPermanent("Feast of Sanity");
            var effect = SequenceEffect.of(new DealDamageToAnyTargetEffect(1), new GainLifeEffect(1));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            assertThat(pending.sourcePermanentId()).isEqualTo(source.getId());
            assertThat(pending.effects()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — SequenceEffect")
    class OpponentDiscardSequence {

        @Test
        @DisplayName("queues one atomic triggered ability for an opponent discard")
        void queuesSequenceTrigger() {
            Permanent nocturnus = createPermanent("Abyssal Nocturnus");
            var effect = SequenceEffect.of(
                    new BoostSelfEffect(2, 2),
                    new GrantKeywordEffect(Keyword.FEAR, GrantScope.SELF));
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(nocturnus, player1Id, effect),
                    LibraryWatch.ON_OPPONENT_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(nocturnus.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — SequenceEffect")
    class ControllerDiscardEventSequence {

        @Test
        @DisplayName("queues a target choice carrying the discard-event count")
        void queuesTargetChoiceWithDiscardedCount() {
            Permanent captain = createPermanent("Captain Howler, Sea Scourge");
            var effect = SequenceEffect.of(
                    new BoostTargetCreatureEffect(2, 0),
                    new RegisterDelayedWatchedCreaturesCombatDamageEffect(List.of(new DrawCardEffect(1))));
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(captain, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            assertThat(pending.discardedCount()).isEqualTo(3);
            assertThat(pending.effects()).containsExactly(effect);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — PutCounterOnEachMatchingPermanentEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — PutCounterOnEachMatchingPermanentEffect")
    class ControllerDiscardPutCounters {

        @Test
        @DisplayName("queues a put-counters triggered ability carrying the source permanent and returns true")
        void queuesPutCountersTrigger() {
            Permanent archfiend = createPermanent("Archfiend of Ifnir");
            var effect = new PutCounterOnEachMatchingPermanentEffect(
                    CounterType.MINUS_ONE_MINUS_ONE, 1,
                    new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(archfiend, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(archfiend.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first()
                    .isInstanceOf(PutCounterOnEachMatchingPermanentEffect.class);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — BoostTargetCreatureEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — BoostTargetCreatureEffect")
    class ControllerDiscardBoostTargetCreature {

        @Test
        @DisplayName("queues a target-creature choice carrying the source permanent and returns true")
        void queuesTargetChoice() {
            Permanent sphinx = createPermanent("Ominous Sphinx");
            var effect = new BoostTargetCreatureEffect(-2, 0, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(sphinx, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)).isTrue();
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            assertThat(pending.controllerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentId()).isEqualTo(sphinx.getId());
            assertThat(pending.effects()).hasSize(1).first().isInstanceOf(BoostTargetCreatureEffect.class);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — MayPayManaEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — MayPayManaEffect")
    class ControllerDiscardMayPayMana {

        @Test
        @DisplayName("queues a may-pay triggered ability for the discarding player and returns true")
        void queuesMayPayManaTrigger() {
            Permanent drakeHaven = createPermanent("Drake Haven");
            var effect = new MayPayManaEffect("{1}", new ScryEffect(1), "Pay {1}?");
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(drakeHaven, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(drakeHaven.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(MayPayManaEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — DrawCardEffect")
    class ControllerDiscardDraw {

        @Test
        @DisplayName("queues a draw trigger for the controller")
        void queuesDrawTrigger() {
            Permanent boneMiser = createPermanent("Bone Miser");
            var effect = new DrawCardEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Spellbook"));

            boolean result = registry.dispatch(
                    match(boneMiser, player1Id, effect),
                    LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(boneMiser.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("Every discard entry records the discard, whatever its effects are")
    class RecordsTheDiscard {

        @Test
        @DisplayName("a \"you may\" records the discarding player, the discarded card and the graveyard stay it went to")
        void mayRecordsTheDiscardedCardAndItsStay() {
            Permanent construct = createPermanent("Containment Construct");
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            long stay = gd.markGraveyardEntry(discarded);
            var effect = new MayEffect(new ExileDiscardedCardFromGraveyardEffect(false, false, true), "Exile it?");
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(construct, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARDS, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getTargetId()).isEqualTo(player1Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getTriggeringCardId()).isEqualTo(discarded.getId());
                assertThat(entry.getTriggeringCardGraveyardEntryVersion()).isEqualTo(stay);
                assertThat(entry.triggeringCardIds()).containsExactly(discarded.getId());
                assertThat(entry.getEventCardGraveyardEntryVersions()).containsEntry(discarded.getId(), stay);
            });
        }

        @Test
        @DisplayName("a \"one or more cards\" ability records every discarded card with its stay and their number")
        void discardEventRecordsEveryCardAndTheirNumber() {
            Permanent toluz = createPermanent("Toluz, Clever Conductor");
            Card first = createCard("Grizzly Bears");
            Card second = createCard("Spellbook");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).addAll(List.of(first, second));
            long firstStay = gd.markGraveyardEntry(first);
            long secondStay = gd.markGraveyardEntry(second);
            var effect = new ExileDiscardedCardFromGraveyardEffect(true);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 2, List.of(first, second));

            boolean result = registry.dispatch(
                    match(toluz, player1Id, effect), LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getSourcePermanentId()).isEqualTo(toluz.getId());
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(2);
                assertThat(entry.triggeringCardIds()).containsExactly(first.getId(), second.getId());
                assertThat(entry.getEventCardGraveyardEntryVersions())
                        .containsEntry(first.getId(), firstStay)
                        .containsEntry(second.getId(), secondStay);
                assertThat(entry.getTriggeringCardId()).isNull();
            });
        }
    }
}
