package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Each head primitive records what it did on the resolving entry's scratchpad, and a later step reads it in the order
 * written (CR 608.2c): "if you do" ({@link HappenedThisWay}), "that many" ({@link AmountThisWay}) and "those
 * permanents" / "that player" ({@code Subjects.thisWay} / {@code Subjects.playersThisWay}). Every case composes the
 * primitives the way a real card's text reads, without the card's own fused effect.
 */
@CardUsed({GrizzlyBears.class, HillGiant.class, Forest.class, GloriousAnthem.class, DarksteelMyr.class,
        BonecrusherGiant.class, DelverOfSecrets.class})
class StepResultsTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private StackEntry resolve(Player controller, UUID targetId, UUID sourcePermanentId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), controller.getId(), "Step results",
                List.of(effects), targetId, sourcePermanentId);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    private final CardPredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
    private final CardPredicate landCard = new CardTypePredicate(CardType.LAND);

    /** Resolves {@code effects} as a triggered ability of player 1 that targets the graveyard card {@code cardId}. */
    private StackEntry resolveTargetingGraveyardCard(UUID cardId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Step results",
                List.of(effects), cardId, Zone.GRAVEYARD);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    /** Resolves {@code effects} as a triggered ability of {@code source}, a card that is not on the battlefield. */
    private StackEntry resolveFor(Card source, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(source, player1.getId(), "Step results", List.of(effects), null,
                (UUID) null);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    private static CardEffect ifDone(StepKind kind, CardEffect then) {
        return new ConditionalEffect(new HappenedThisWay(kind), then);
    }

    @Nested
    @DisplayName("Draw")
    class Draw {

        @Test
        @DisplayName("Baral, Chief of Compliance: \"you may draw a card. If you do, discard a card\" discards after a draw")
        void discardsAfterADraw() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new DiscardEffect(1, Subjects.you(), true)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW).amount()).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("A draw from an empty library draws nothing, so its \"if you do\" step does nothing")
        void nothingDrawnFromAnEmptyLibrary() {
            harness.setLibrary(player1, List.of());
            harness.setHand(player1, List.of(new GrizzlyBears()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new DiscardEffect(1, Subjects.you(), true)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW)).isEqualTo(StepOutcome.NONE);
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        }

        @Test
        @DisplayName("The Royal Scions: \"draw four cards. When you do\" needs all four drawn (fewer don't count)")
        void fewerCardsThanWrittenDoNotCount() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

            resolve(player1, null, null, new DrawCardEffect(4),
                    new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DRAW, 4), new GainLifeEffect(5)));

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("The Royal Scions: four cards drawn meet \"draw four cards. When you do\"")
        void theWrittenNumberCounts() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

            resolve(player1, null, null, new DrawCardEffect(4),
                    new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DRAW, 4), new GainLifeEffect(5)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Discard")
    class Discard {

        @Test
        @DisplayName("Mishra's Command: \"they draw a card for each card discarded this way\" counts the chosen discard")
        void drawsForEachCardDiscardedAfterTheChoice() {
            harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));
            harness.setLibrary(player1, List.of(new Forest(), new Forest()));

            StackEntry entry = resolve(player1, null, null, new DiscardEffect(1, Subjects.you(), false),
                    new DrawCardEffect(new AmountThisWay(StepKind.DISCARD)));
            harness.handleCardChosen(player1, 0);

            assertThat(entry.getResolutionResults().outcome(StepKind.DISCARD).amount()).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                    .containsExactlyInAnyOrder("Hill Giant", "Forest");
        }

        @Test
        @DisplayName("Pack Guardian: \"discard a land card\" offers only the land cards in hand")
        void filteredDiscardOffersOnlyMatchingCards() {
            harness.setHand(player1, List.of(new GrizzlyBears(), new Forest(), new HillGiant()));

            resolve(player1, null, null,
                    DiscardEffect.matching(1, Subjects.you(), new CardTypePredicate(CardType.LAND)));

            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).validIndices())
                    .containsExactly(1);
        }

        @Test
        @DisplayName("Recruit: \"if you discarded a nonland card\" reads the card the discard step discarded")
        void discardedCardIsJudgedByItsType() {
            harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

            resolve(player1, null, null, new DiscardEffect(1, Subjects.you()),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD,
                            new CardTypePredicate(CardType.CREATURE)), new GainLifeEffect(3)));
            harness.handleCardChosen(player1, 1);

            harness.assertLife(player1, 23);
        }

        @Test
        @DisplayName("A discarded land card is not a creature card discarded this way")
        void discardedLandIsNotACreatureCard() {
            harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

            resolve(player1, null, null, new DiscardEffect(1, Subjects.you()),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD,
                            new CardTypePredicate(CardType.CREATURE)), new GainLifeEffect(3)));
            harness.handleCardChosen(player1, 0);

            harness.assertLife(player1, 20);
        }

        @Test
        @DisplayName("Discarding from an empty hand discards nothing, so nothing is drawn for it")
        void emptyHandDrawsNothing() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new DiscardEffect(2, Subjects.you(), true),
                    new DrawCardEffect(new AmountThisWay(StepKind.DISCARD)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mill")
    class Mill {

        @Test
        @DisplayName("Saprazzan Breaker: \"mill a card. If a land card was milled this way\" reads the milled card")
        void millRecordsTheMilledCard() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new MillEffect(1, Subjects.you()),
                    ifDone(StepKind.MILL, new GainLifeEffect(1)));

            StepOutcome milled = entry.getResolutionResults().outcome(StepKind.MILL);
            assertThat(milled.amount()).isEqualTo(1);
            assertThat(milled.playerIds()).containsExactly(player1.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }

        @Test
        @DisplayName("Milling more cards than the library holds mills as many as possible (CR 701.17b)")
        void millsAsManyAsPossible() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new MillEffect(3, Subjects.you()),
                    new GainLifeEffect(new AmountThisWay(StepKind.MILL)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("Exile")
    class Exile {

        @Test
        @DisplayName("Exile: \"exile target creature. You gain life\" records the exiled creature and its controller")
        void recordsTheExiledCreatureAndItsController() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null, new ExileTargetPermanentEffect(),
                    new GainLifeEffect(2, Subjects.playersThisWay(StepKind.EXILE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds()).containsExactly(bears.getId());
            assertThat(gd.getLife(player2.getId())).isEqualTo(22);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Misfortune Teller: \"exile target card from a graveyard. If it was a creature card\" reads the card")
        void graveyardExileRecordsTheCardAndItsOwner() {
            Card bears = new GrizzlyBears();
            gd.playerGraveyards.get(player2.getId()).add(bears);

            StackEntry entry = resolveTargetingGraveyardCard(bears.getId(),
                    ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, creatureCard), new GainLifeEffect(3)),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, landCard), new GainLifeEffect(5)));

            StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
            assertThat(exiled.objectIds()).containsExactly(bears.getId());
            assertThat(exiled.playerIds()).containsExactly(player2.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        }

        @Test
        @DisplayName("Klothys, God of Destiny: a land card exiled from a graveyard isn't a creature card exiled this way")
        void exiledLandIsNotACreatureCard() {
            Card forest = new Forest();
            gd.playerGraveyards.get(player2.getId()).add(forest);

            resolveTargetingGraveyardCard(forest.getId(),
                    ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, creatureCard), new GainLifeEffect(3)),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, landCard), new GainLifeEffect(5)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        }

        @Test
        @DisplayName("Rooting Kavu: \"exile it. If you do\" happens when the source's card is exiled from its graveyard")
        void sourceCardExiledFromItsGraveyard() {
            Card kavu = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(kavu);

            StackEntry entry = resolveFor(kavu, new ExileSourceCardFromGraveyardEffect(),
                    ifDone(StepKind.EXILE, new GainLifeEffect(2)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds()).containsExactly(kavu.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("A source card that already left its graveyard is not exiled this way, so \"if you do\" does nothing")
        void sourceCardGoneFromItsGraveyard() {
            Card kavu = new GrizzlyBears();

            StackEntry entry = resolveFor(kavu, new ExileSourceCardFromGraveyardEffect(),
                    ifDone(StepKind.EXILE, new GainLifeEffect(2)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE)).isEqualTo(StepOutcome.NONE);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Bioplasm: the top card of a library exiled this way gives its power, toughness and mana value")
        void libraryExileGivesTheCardsStats() {
            harness.setLibrary(player1, List.of(new HillGiant()));

            StackEntry entry = resolve(player1, null, null, new ExileTopCardOfOwnLibraryEffect(false),
                    new GainLifeEffect(AmountThisWay.exiledPower()),
                    new GainLifeEffect(AmountThisWay.exiledToughness()),
                    new LoseLifeEffect(AmountThisWay.exiledManaValue(), Subjects.eachOpponent()));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).playerIds()).containsExactly(player1.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(26);
            assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        }

        @Test
        @DisplayName("Daxos of Meletis: \"that player\" exiles their top card; its owner is recorded and only you may cast it")
        void anotherPlayersLibraryExileRecordsTheOwnerAndGrantsTheController() {
            Card giant = new HillGiant();
            harness.setLibrary(player2, List.of(giant));

            StackEntry entry = resolve(player1, player2.getId(), null,
                    new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                            .mayOnlyCast().withAnyManaType(),
                    new GainLifeEffect(AmountThisWay.exiledManaValue(), Subjects.you()));

            StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
            assertThat(exiled.objectIds()).containsExactly(giant.getId());
            assertThat(exiled.playerIds()).containsExactly(player2.getId());
            assertThat(gd.exilePlayPermissionHolder(giant.getId())).isEqualTo(player1.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        }

        @Test
        @DisplayName("A land card exiled by a \"you may cast that card\" step gets no permission and gives no life")
        void castOnlyLibraryExileGivesALandNoPermission() {
            Card forest = new Forest();
            harness.setLibrary(player2, List.of(forest));

            resolve(player1, player2.getId(), null,
                    new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                            .mayOnlyCast(),
                    new GainLifeEffect(AmountThisWay.exiledManaValue(), Subjects.you()));

            assertThat(gd.findExiledCard(forest.getId())).isNotNull();
            assertThat(gd.exilePlayPermission(forest.getId())).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Jaya, Fiery Negotiator: \"choose one of them\" ends the exile step on the choice, and a later step runs after it")
        void chooseOneOfThemEndsTheStepOnTheChoice() {
            Card giant = new HillGiant();
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(giant, forest));

            StackEntry entry = resolve(player1, null, null,
                    new ExileTopCardMayPlayThisTurnEffect(2, false).choosingOne(),
                    new GainLifeEffect(new AmountThisWay(StepKind.EXILE), Subjects.you()));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds())
                    .containsExactly(giant.getId(), forest.getId());
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
            assertThat(gd.exilePlayPermissionHolders()).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);

            harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

            assertThat(gd.exilePlayPermissionHolders()).containsOnlyKeys(forest.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Chandra, Hope's Beacon: a cast filter permits an Adventure card whose Adventure matches, and no other card")
        void castFilterPermitsAMatchingAdventure() {
            Card giant = new BonecrusherGiant();
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(giant, bears, forest));
            CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));

            resolve(player1, null, null,
                    new ExileTopCardMayPlayThisTurnEffect(3, false).mayOnlyCast(instantOrSorcery));

            assertThat(gd.exilePlayPermissionHolders()).containsOnlyKeys(giant.getId());
            assertThat(gd.exilePlayPermission(giant.getId()).orElseThrow().spellFilter()).isEqualTo(instantOrSorcery);
        }

        @Test
        @DisplayName("Scrib Nibblers: target player's top card exiled this way is judged as a land card by its owner's step")
        void targetPlayersLibraryExileIsJudgedByType() {
            Card forest = new Forest();
            harness.setLibrary(player2, List.of(forest));

            StackEntry entry = resolve(player1, player2.getId(), null,
                    ExileTopCardOfOwnLibraryEffect.of(Subjects.targetPlayer()),
                    new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, landCard), new GainLifeEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).playerIds())
                    .containsExactly(player2.getId());
            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }

        @Test
        @DisplayName("Swords to Plowshares: an exiled creature's power is its power as it last existed on the battlefield")
        void exiledPermanentGivesItsLastKnownPower() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            resolve(player1, bears.getId(), null, new ExileTargetPermanentEffect(),
                    new GainLifeEffect(AmountThisWay.exiledPower(), Subjects.playersThisWay(StepKind.EXILE)));

            assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        }

        @Test
        @DisplayName("A graveyard card chosen at the exile prompt is the step's result, and the rider runs once after it")
        void ownGraveyardExileChosenAtAPromptIsTheStepResult() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(bears, forest, giant));

            StackEntry entry = resolve(player1, null, null,
                    ExileGraveyardCardsEffect.fromOwnGraveyard(1, creatureCard),
                    ifDone(StepKind.EXILE, new GainLifeEffect(1)));

            var choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
            assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 2);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
            harness.handleGraveyardCardChosen(player1, 2);

            StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
            assertThat(exiled.objectIds()).containsExactly(giant.getId());
            assertThat(exiled.playerIds()).containsExactly(player1.getId());
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, forest);
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("Entrails Feaster: a card chosen from any graveyard is the step's result; declining exiles nothing this way")
        void anyGraveyardExileChosenAtResolutionIsTheStepResult() {
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setGraveyard(player2, List.of(forest, bears));

            StackEntry entry = resolve(player1, null, null,
                    ExileUpToOneCardFromGraveyardEffect.matching(creatureCard),
                    ifDone(StepKind.EXILE, new GainLifeEffect(1)));

            var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice).isNotNull();
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

            StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
            assertThat(exiled.objectIds()).containsExactly(bears.getId());
            assertThat(exiled.playerIds()).containsExactly(player2.getId());
            assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);

            Card giant = new HillGiant();
            harness.setGraveyard(player2, List.of(forest, giant));
            resolve(player1, null, null,
                    ExileUpToOneCardFromGraveyardEffect.matching(creatureCard),
                    ifDone(StepKind.EXILE, new GainLifeEffect(1)));
            harness.handleMultipleCardsChosen(player1, List.of());

            assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest, giant);
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("A lone matching graveyard card is exiled without a prompt; with none, nothing is exiled this way")
        void ownGraveyardExileOfALoneMatchOrNone() {
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(forest, bears));

            StackEntry entry = resolve(player1, null, null,
                    ExileGraveyardCardsEffect.fromOwnGraveyard(1, creatureCard),
                    ifDone(StepKind.EXILE, new GainLifeEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds()).containsExactly(bears.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);

            resolve(player1, null, null,
                    ExileGraveyardCardsEffect.fromOwnGraveyard(1, creatureCard),
                    ifDone(StepKind.EXILE, new GainLifeEffect(1)));

            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }

        @Test
        @DisplayName("Each later pick of a multi-card graveyard exile offers only the matching cards still there")
        void multiCardOwnGraveyardExileKeepsItsFilter() {
            Card first = new GrizzlyBears();
            Card second = new HillGiant();
            Card third = new GrizzlyBears();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(first, forest, second, third));

            StackEntry entry = resolve(player1, null, null,
                    ExileGraveyardCardsEffect.fromOwnGraveyard(2, creatureCard),
                    new GainLifeEffect(new AmountThisWay(StepKind.EXILE), Subjects.you()));

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).validIndices())
                    .containsExactlyInAnyOrder(0, 2, 3);
            harness.handleGraveyardCardChosen(player1, 0);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).validIndices())
                    .containsExactlyInAnyOrder(1, 2);
            harness.handleGraveyardCardChosen(player1, 1);

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds())
                    .containsExactly(first.getId(), second.getId());
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, third);
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("A library exile's permission lasts its duration, face down and paid for with life as it says")
        void libraryExilePermissionKeepsItsDurationFaceDownAndLifePayment() {
            Card giant = new HillGiant();
            harness.setLibrary(player2, List.of(giant));

            StackEntry entry = resolve(player1, player2.getId(), null,
                    new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                            .exiledFaceDown().payingLifeEqualToManaValue().until(Duration.PERMANENT));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds()).containsExactly(giant.getId());
            assertThat(gd.findExiledCard(giant.getId()).faceDown()).isTrue();
            assertThat(gd.exilePlayPermissionHolder(giant.getId())).isEqualTo(player1.getId());
            assertThat(gd.exilePlayPermissionDuration(giant.getId())).isEqualTo(Duration.PERMANENT);
            assertThat(gd.exilePlayForLifeCardIds()).contains(giant.getId());
        }

        @Test
        @DisplayName("Malboro: each opponent exiles the top three cards of their library, all of them this step's result")
        void eachOpponentExilesSeveralLibraryCards() {
            Card first = new GrizzlyBears();
            Card second = new HillGiant();
            Card third = new Forest();
            Card kept = new Forest();
            harness.setLibrary(player2, List.of(first, second, third, kept));

            StackEntry entry = resolve(player1, null, null,
                    ExileTopCardOfOwnLibraryEffect.of(Subjects.eachOpponent(), 3));

            StepOutcome exiled = entry.getResolutionResults().outcome(StepKind.EXILE);
            assertThat(exiled.objectIds()).containsExactly(first.getId(), second.getId(), third.getId());
            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(kept);
            assertThat(gd.exilePlayPermission(first.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Destroy")
    class Destroy {

        @Test
        @DisplayName("Break the Spell: \"destroy target enchantment. If ... was destroyed this way, draw a card\"")
        void drawsWhenTheTargetIsDestroyed() {
            Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, anthem.getId(), null, new DestroyTargetPermanentEffect(),
                    ifDone(StepKind.DESTROY, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("An indestructible target is not destroyed this way, so nothing is drawn")
        void indestructibleTargetIsNotDestroyed() {
            Permanent myr = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, myr.getId(), null, new DestroyTargetPermanentEffect(),
                    ifDone(StepKind.DESTROY, new DrawCardEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DESTROY).happened()).isFalse();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Sacrifice")
    class Sacrifice {

        @Test
        @DisplayName("Victimize: \"sacrifice a creature. If you do, ...\" reads the sacrifice")
        void readsTheSacrifice() {
            harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    ifDone(StepKind.SACRIFICE, new DrawCardEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).playerIds())
                    .containsExactly(player1.getId());
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("With no creature to sacrifice, the \"if you do\" step does nothing")
        void nothingToSacrifice() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    ifDone(StepKind.SACRIFICE, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("A lone eligible creature is sacrificed without a prompt and its last-known power and mana value are read")
        void autoPickRecordsLastKnownStats() {
            harness.addToBattlefield(player1, new GloriousAnthem());
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            int lifeBefore = gd.getLife(player1.getId());

            StackEntry entry = resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    new GainLifeEffect(AmountThisWay.sacrificedPower()),
                    new LoseLifeEffect(AmountThisWay.sacrificedManaValue(), Subjects.you()));

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).objectIds())
                    .containsExactly(giant.getId());
            // Hill Giant is 4/4 under Glorious Anthem as it is sacrificed; its mana value is 4.
            harness.assertLife(player1, lifeBefore + 4 - 4);
            assertThat(entry.getResolutionResults().departures()).singleElement()
                    .satisfies(departure -> {
                        assertThat(departure.lastKnownPower()).isEqualTo(4);
                        assertThat(departure.lastKnownToughness()).isEqualTo(4);
                        assertThat(departure.lastKnownManaValue()).isEqualTo(4);
                    });
        }

        @Test
        @DisplayName("The creature chosen at the sacrifice prompt gives \"that creature's toughness\"")
        void promptAnswerRecordsTheChosenCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            int lifeBefore = gd.getLife(player1.getId());

            StackEntry entry = resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    new GainLifeEffect(AmountThisWay.sacrificedToughness()));
            harness.handlePermanentChosen(player1, giant.getId());

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).objectIds())
                    .containsExactly(giant.getId());
            harness.assertLife(player1, lifeBefore + 3);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Each player chooses, then all sacrifice together, and the step records every sacrificed creature")
        void eachPlayerChoosesThenSacrificesTogether() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            int lifeBefore = gd.getLife(player1.getId());

            StackEntry entry = resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.eachPlayer()),
                    new GainLifeEffect(AmountThisWay.sacrificedPower()));
            harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).objectIds())
                    .containsExactlyInAnyOrder(giant.getId(), opposingBears.getId());
            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).playerIds())
                    .containsExactlyInAnyOrder(player1.getId(), player2.getId());
            harness.assertLife(player1, lifeBefore + 3 + 2);
        }

        @Test
        @DisplayName("\"If you sacrifice a Bear this way\" judges the sacrificed creature as it last existed")
        void filteredConditionReadsTheSacrificedCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    new ConditionalEffect(HappenedThisWay.toA(StepKind.SACRIFICE,
                            new PermanentHasSubtypePredicate(CardSubtype.BEAR)), new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("A sacrificed creature that isn't a Bear fails \"if you sacrifice a Bear this way\"")
        void filteredConditionFailsForAnotherType() {
            harness.addToBattlefield(player1, new HillGiant());
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    new ConditionalEffect(HappenedThisWay.toA(StepKind.SACRIFICE,
                            new PermanentHasSubtypePredicate(CardSubtype.BEAR)), new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("A \"when you do\" reflexive ability reads the sacrificed creature's power")
        void reflexiveAbilityReadsTheSacrifice() {
            harness.addToBattlefield(player1, new HillGiant());
            int lifeBefore = gd.getLife(player1.getId());

            resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    ifDone(StepKind.SACRIFICE, new QueueReflexiveAbilityEffect(
                            new GainLifeEffect(AmountThisWay.sacrificedPower()))));
            harness.passBothPriorities();

            harness.assertLife(player1, lifeBefore + 3);
        }

        @Test
        @DisplayName("Reprocess: \"sacrifice any number of ... Draw a card for each permanent sacrificed this way\" counts the chosen ones")
        void anyNumberRecordsTheChosenPermanents() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

            StackEntry entry = resolve(player1, null, null,
                    SacrificePermanentsEffect.anyNumber(new PermanentIsCreaturePredicate(), Subjects.you()),
                    new DrawCardEffect(new AmountThisWay(StepKind.SACRIFICE)));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                    .containsExactlyInAnyOrder(bears.getId(), giant.getId());
            harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), giant.getId()));

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).objectIds())
                    .containsExactlyInAnyOrder(bears.getId(), giant.getId());
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        }

        @Test
        @DisplayName("\"Any number\" includes none: a lone candidate is still asked about, and keeping it sacrifices nothing")
        void anyNumberMayChooseNone() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null,
                    SacrificePermanentsEffect.anyNumber(new PermanentIsCreaturePredicate(), Subjects.you()),
                    new DrawCardEffect(new AmountThisWay(StepKind.SACRIFICE)));
            harness.handleMultiplePermanentsChosen(player1, List.of());

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).happened()).isFalse();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Manifest")
    class Manifest {

        @Test
        @DisplayName("Formless Nurturing: \"manifest the top card of your library, then put a +1/+1 counter on it\"")
        void countersGoOnTheManifestedPermanent() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null,
                    new ManifestTopCardEffect(),
                    new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.MANIFEST),
                            CounterType.PLUS_ONE_PLUS_ONE));

            UUID manifestedId = entry.getResolutionResults().outcome(StepKind.MANIFEST).objectIds().getFirst();
            Permanent manifested = gqs.findPermanentById(gd, manifestedId);
            assertThat(manifested.isFaceDown()).isTrue();
            assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(entry.getResolutionResults().outcome(StepKind.MANIFEST).playerIds())
                    .containsExactly(player1.getId());
        }

        @Test
        @DisplayName("With an empty library nothing is manifested this way, so no counter is put on anything")
        void emptyLibraryManifestsNothing() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of());

            StackEntry entry = resolve(player1, null, null,
                    new ManifestTopCardEffect(),
                    new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.MANIFEST),
                            CounterType.PLUS_ONE_PLUS_ONE));

            assertThat(entry.getResolutionResults().outcome(StepKind.MANIFEST).happened()).isFalse();
            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }

    @Nested
    @DisplayName("Transform")
    class Transform {

        @Test
        @DisplayName("Cyclonus, Cybertronian Fighter: \"convert it. If you do\" happens after the source transformed")
        void ifYouDoFollowsTheTransform() {
            Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

            StackEntry entry = resolve(player1, null, delver.getId(),
                    new TransformSelfEffect(),
                    new ConditionalEffect(HappenedThisWay.transformed(), new GainLifeEffect(3)));

            assertThat(delver.isTransformed()).isTrue();
            assertThat(gd.getLife(player1.getId())).isEqualTo(23);
            assertThat(entry.getResolutionResults().outcome(StepKind.TRANSFORM).objectIds())
                    .containsExactly(delver.getId());
            assertThat(entry.getResolutionResults().outcome(StepKind.TRANSFORM).playerIds())
                    .containsExactly(player1.getId());
        }

        @Test
        @DisplayName("A source with no other face doesn't transform, so its \"if you do\" step does nothing")
        void noBackFaceMeansNothingTransformed() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            StackEntry entry = resolve(player1, null, bears.getId(),
                    new TransformSelfEffect(),
                    new ConditionalEffect(HappenedThisWay.transformed(), new GainLifeEffect(3)));

            assertThat(entry.getResolutionResults().outcome(StepKind.TRANSFORM).happened()).isFalse();
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Return")
    class Return {

        @Test
        @DisplayName("Recoil: \"return target permanent to its owner's hand. Then that player discards a card\"")
        void thatPlayerIsTheOwner() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null, ReturnToHandEffect.target(),
                    new DiscardEffect(1, Subjects.playersThisWay(StepKind.RETURN), true));

            assertThat(entry.getResolutionResults().outcome(StepKind.RETURN).playerIds())
                    .containsExactly(player2.getId());
            assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
        }

        @Test
        @DisplayName("A permanent chosen at the return prompt is the step's result, and \"if you do\" runs after the choice")
        void chosenReturnEndsTheStepOnTheChoice() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new HillGiant());
            harness.setLife(player1, 10);

            StackEntry entry = resolve(player1, null, null,
                    ReturnPermanentControlledByPlayerToHandEffect.controller(new PermanentIsCreaturePredicate(),
                            "creature"),
                    ifDone(StepKind.RETURN, new GainLifeEffect(3)));
            harness.assertLife(player1, 10);
            harness.handlePermanentChosen(player1, bears.getId());

            assertThat(entry.getResolutionResults().outcome(StepKind.RETURN).objectIds())
                    .containsExactly(bears.getId());
            assertThat(entry.getResolutionResults().outcome(StepKind.RETURN).playerIds())
                    .containsExactly(player1.getId());
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertLife(player1, 13);
        }

        @Test
        @DisplayName("With nothing to choose, nothing is returned this way and \"if you do\" does nothing")
        void nothingToReturnIsNoReturn() {
            harness.setLife(player1, 10);

            StackEntry entry = resolve(player1, null, null,
                    ReturnPermanentControlledByPlayerToHandEffect.controller(new PermanentIsCreaturePredicate(),
                            "creature"),
                    ifDone(StepKind.RETURN, new GainLifeEffect(3)));

            assertThat(entry.getResolutionResults().outcome(StepKind.RETURN)).isEqualTo(StepOutcome.NONE);
            harness.assertLife(player1, 10);
        }

        @Test
        @DisplayName("\"If a Bear was returned this way\" judges the returned permanent as it last existed")
        void returnedPermanentIsJudgedAsItLastExisted() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            harness.setLife(player1, 10);
            CardEffect gainIfBear = new ConditionalEffect(
                    HappenedThisWay.toA(StepKind.RETURN, new PermanentHasSubtypePredicate(CardSubtype.BEAR)),
                    new GainLifeEffect(3));
            CardEffect returnCreature = ReturnPermanentControlledByPlayerToHandEffect.controller(
                    new PermanentIsCreaturePredicate(), "creature");

            resolve(player1, null, null, returnCreature, gainIfBear);
            harness.handlePermanentChosen(player1, giant.getId());
            harness.assertLife(player1, 10);

            resolve(player1, null, null, returnCreature, gainIfBear);
            harness.handlePermanentChosen(player1, bears.getId());
            harness.assertLife(player1, 13);
        }

        @Test
        @DisplayName("\"Return one of them\": the controller chooses only among the permanents the subject names")
        void choosingOneOfOffersOnlyTheSubjectsPermanents() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new HillGiant());

            resolve(player1, null, null, ReturnPermanentControlledByPlayerToHandEffect.choosingOneOf(
                    Subjects.ownedBy(Subjects.you(), new PermanentIsCreaturePredicate()), "creature you own"));

            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactly(bears.getId());
            harness.handlePermanentChosen(player1, bears.getId());
            harness.assertInHand(player1, "Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("Create token")
    class CreateToken {

        @Test
        @DisplayName("Turntimber Ranger: \"create a 2/2 green Wolf creature token. If you do, put a +1/+1 counter\"")
        void putsACounterAfterTheToken() {
            Permanent ranger = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            StackEntry entry = resolve(player1, null, ranger.getId(),
                    new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()),
                    ifDone(StepKind.CREATE_TOKEN, new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.CREATE_TOKEN).amount()).isEqualTo(1);
            assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Deal damage")
    class DealDamage {

        @Test
        @DisplayName("Syphon Soul: \"2 damage to each other player. You gain life equal to the damage dealt this way\"")
        void gainsTheDamageDealt() {
            resolve(player1, null, null, new DealDamageToPlayersEffect(2, Subjects.eachOpponent()),
                    new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

            assertThat(gd.getLife(player2.getId())).isEqualTo(18);
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Provoke the Trolls: \"if a creature is dealt damage this way, it gets +5/+0\"")
        void boostsTheCreatureDealtDamage() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            StackEntry entry = resolve(player1, giant.getId(), null,
                    new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new BoostSelfEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE), 5, 0));

            assertThat(entry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE).objectIds())
                    .containsExactly(giant.getId());
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(8);
        }

        @Test
        @DisplayName("Damage to a player is no creature dealt damage this way: the boost names nothing")
        void playerDamageNamesNoCreature() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            resolve(player1, player2.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(3), false, false),
                    new BoostSelfEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE), 5, 0));

            assertThat(gd.getLife(player2.getId())).isEqualTo(17);
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        }

        @Test
        @DisplayName("The Black Arrow: \"if a Dragon is dealt damage this way, destroy it\" spares a creature of another type")
        void filteredSubjectNamesOnlyMatchingPermanents() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            resolve(player1, giant.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE,
                            new PermanentHasSubtypePredicate(CardSubtype.DRAGON))));

            harness.assertOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @DisplayName("A filtered step result names the matching permanent dealt damage: a Giant dealt damage this way is destroyed")
        void filteredSubjectNamesTheMatchingPermanent() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            resolve(player1, giant.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE,
                            new PermanentHasSubtypePredicate(CardSubtype.GIANT))));

            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @DisplayName("Marauding Raptor: \"if a Dinosaur is dealt damage this way\" holds only for a matching permanent")
        void happenedToAMatchingPermanent() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            resolve(player1, giant.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new ConditionalEffect(HappenedThisWay.toA(StepKind.DEAL_DAMAGE,
                            new PermanentHasSubtypePredicate(CardSubtype.GIANT)), new GainLifeEffect(2)),
                    new ConditionalEffect(HappenedThisWay.toA(StepKind.DEAL_DAMAGE,
                            new PermanentHasSubtypePredicate(CardSubtype.DRAGON)), new GainLifeEffect(5)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Damage to a player is no permanent dealt damage this way: a filtered condition fails")
        void playerDamageFailsAFilteredCondition() {
            resolve(player1, player2.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new ConditionalEffect(HappenedThisWay.toA(StepKind.DEAL_DAMAGE,
                            new PermanentIsCreaturePredicate()), new GainLifeEffect(2)));

            assertThat(gd.getLife(player2.getId())).isEqualTo(19);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Gain and lose life")
    class Life {

        @Test
        @DisplayName("Gonti's Machinations: \"each opponent loses 3 life. You gain life equal to the life lost this way\"")
        void gainsTheLifeLost() {
            StackEntry entry = resolve(player1, null, null, new LoseLifeEffect(3, Subjects.eachOpponent()),
                    new GainLifeEffect(new AmountThisWay(StepKind.LOSE_LIFE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.LOSE_LIFE).playerIds())
                    .containsExactly(player2.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(23);
            assertThat(entry.getResolutionResults().outcome(StepKind.GAIN_LIFE).amount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Gaining 0 life is no life gained this way")
        void zeroLifeGainedDidNotHappen() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new GainLifeEffect(0),
                    ifDone(StepKind.GAIN_LIFE, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Counters")
    class Counters {

        @Test
        @DisplayName("Putting counters on target creature records how many and on which creature")
        void putCountersRecordsThem() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null,
                    new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                    new GainLifeEffect(new AmountThisWay(StepKind.PUT_COUNTERS)));

            assertThat(entry.getResolutionResults().outcome(StepKind.PUT_COUNTERS).objectIds())
                    .containsExactly(bears.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Living Artifact: \"remove a vitality counter from it. If you do, you gain 1 life\"")
        void gainsOnlyWhenACounterCameOff() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.VITALITY, 1);
            CardEffect remove = new RemoveCounterFromTargetPermanentEffect(CounterType.VITALITY, null);

            resolve(player1, bears.getId(), null, remove, ifDone(StepKind.REMOVE_COUNTERS, new GainLifeEffect(1)));
            resolve(player1, bears.getId(), null, remove, ifDone(StepKind.REMOVE_COUNTERS, new GainLifeEffect(1)));

            assertThat(bears.getCounterCount(CounterType.VITALITY)).isZero();
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("Vote")
    class Votes {

        private StackEntry vote(String first, String second, CardEffect... readers) {
            List<CardEffect> effects = new java.util.ArrayList<>();
            effects.add(VoteEffect.startingWithYou("Yes", "No"));
            effects.addAll(List.of(readers));
            StackEntry entry = resolve(player1, null, null, effects.toArray(CardEffect[]::new));
            harness.handleListChoice(player1, first);
            harness.handleListChoice(player2, second);
            return entry;
        }

        @Test
        @DisplayName("A vote records each vote, who cast it and for what, in voting order")
        void recordsEachVote() {
            StackEntry entry = vote("Yes", "No");

            StepOutcome outcome = entry.getResolutionResults().outcome(StepKind.VOTE);
            assertThat(outcome.amount()).isEqualTo(2);
            assertThat(outcome.votes()).containsExactly(new StepOutcome.Vote(player1.getId(), "Yes"),
                    new StepOutcome.Vote(player2.getId(), "No"));
            assertThat(outcome.playerIds()).containsExactly(player1.getId(), player2.getId());
        }

        @Test
        @DisplayName("\"For each yes vote\" counts the votes for that option")
        void countsTheVotesForAnOption() {
            vote("Yes", "Yes", new GainLifeEffect(new VotesFor("Yes")), new LoseLifeEffect(new VotesFor("No"),
                    Subjects.you()));

            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("A 1-1 vote is tied: \"more votes\" fails for both, \"more votes or tied\" holds")
        void aSplitVoteIsTied() {
            vote("Yes", "No",
                    new ConditionalEffect(VoteResult.moreVotes("Yes"), new GainLifeEffect(1)),
                    new ConditionalEffect(VoteResult.moreVotes("No"), new GainLifeEffect(2)),
                    new ConditionalEffect(VoteResult.moreVotesOrTied("No"), new GainLifeEffect(4)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        }

        @Test
        @DisplayName("An option with every vote gets more votes; the other neither gets more nor ties, nor has any")
        void aUnanimousVoteIsNotTied() {
            vote("Yes", "Yes",
                    new ConditionalEffect(VoteResult.moreVotes("Yes"), new GainLifeEffect(1)),
                    new ConditionalEffect(VoteResult.moreVotesOrTied("No"), new GainLifeEffect(2)),
                    new ConditionalEffect(VoteResult.anyVotes("No"), new GainLifeEffect(4)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("Reading rules")
    class Reading {

        @Test
        @DisplayName("\"If you don't\" is the negation: it applies when the earlier step did nothing")
        void ifYouDont() {
            harness.setLibrary(player1, List.of());

            resolve(player1, null, null, new DrawCardEffect(1),
                    new ConditionalEffect(new NotCondition(HappenedThisWay.drew()), new GainLifeEffect(2)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("A later step of the same kind replaces the earlier step's result, even when it did nothing")
        void aLaterStepReplacesTheEarlierResult() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1), new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new GainLifeEffect(5)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW)).isEqualTo(StepOutcome.NONE);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("A subject that duplicates an existing step-result subject is refused")
        void duplicateSubjectsAreRefused() {
            assertThatThrownBy(() -> Subjects.thisWay(StepKind.CREATE_TOKEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("createdThisResolution");
            assertThatThrownBy(() -> Subjects.thisWay(StepKind.DESTROY))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Subjects.playersThisWay(StepKind.DEAL_DAMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("playersDealtDamageThisResolution");
        }
    }
}
