package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.s.SentinelTotem;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({KarnScionOfUrza.class, GrizzlyBears.class, Forest.class, ShortSword.class,
        PullFromEternity.class, SentinelTotem.class})
class KarnScionOfUrzaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new KarnScionOfUrza(), "{4}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Karn on battlefield with loyalty 5")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.castFromHand(player1, new KarnScionOfUrza(), "{4}");
        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Karn, Scion of Urza"));
        Permanent karn = findPermanent(player1, "Karn, Scion of Urza");
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(karn.isSummoningSick()).isFalse();
    }

    @Nested
    @DisplayName("+1 ability")
    @CardUsed({KarnScionOfUrza.class, GrizzlyBears.class, Forest.class})
    class PlusOneAbility {

        @Test
        @DisplayName("+1 increases loyalty and presents opponent with choice")
        void plusOneIncreasesLoyaltyAndPresentsChoice() {
            Permanent karn = addReadyKarn(player1);
            // Put known cards on top of library
            harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        }

        @Test
        @DisplayName("Opponent chooses a card — chosen goes to controller's hand, other exiled with silver counter")
        void opponentChoosesCardForHand() {
            addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(bears, forest));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Opponent (player2) chooses Grizzly Bears for the controller's hand
            harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

            // Grizzly Bears should be in controller's hand
            harness.assertInHand(player1, "Grizzly Bears");

            // Forest should be exiled with a silver counter
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
            assertThat(gd.exiledCardsWithSilverCounters).contains(forest.getId());
        }

        @Test
        @DisplayName("Opponent can choose the other card instead")
        void opponentChoosesOtherCard() {
            addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(bears, forest));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Opponent chooses Forest for the controller's hand
            harness.handleMultipleCardsChosen(player2, List.of(forest.getId()));

            // Forest in controller's hand
            harness.assertInHand(player1, "Forest");

            // Grizzly Bears exiled with silver counter
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exiledCardsWithSilverCounters).contains(bears.getId());
        }

        @Test
        @DisplayName("+1 with only one card in library puts it into hand")
        void plusOneWithOneCardInLibrary() {
            addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            harness.setLibrary(player1, List.of(bears));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Only one card — goes directly to hand, no opponent choice
            assertThat(gd.interaction.activeInteraction()).isNull();
            harness.assertInHand(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("+1 with empty library does nothing")
        void plusOneWithEmptyLibrary() {
            Permanent karn = addReadyKarn(player1);
            harness.setLibrary(player1, List.of());

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // Loyalty still increased
        }
    }

    @Nested
    @DisplayName("−1 ability")
    @CardUsed({KarnScionOfUrza.class, GrizzlyBears.class, Forest.class})
    class MinusOneAbility {

        @Test
        @DisplayName("−1 returns a silver-countered card from exile to hand")
        void minusOneReturnsSilverCounterCard() {
            Permanent karn = addReadyKarn(player1);
            Card exiledCard = new GrizzlyBears();

            // Set up exiled card with silver counter
            gd.addToExile(player1.getId(), exiledCard);
            gd.exiledCardsWithSilverCounters.add(exiledCard.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 5 - 1
            // Card should be in hand
            harness.assertInHand(player1, "Grizzly Bears");
            // Card should no longer be in exile
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            // Silver counter tracking should be removed
            assertThat(gd.exiledCardsWithSilverCounters).doesNotContain(exiledCard.getId());
        }

        @Test
        @DisplayName("−1 with multiple silver-counter cards presents choice")
        void minusOneWithMultipleCardsPresentsChoice() {
            addReadyKarn(player1);
            Card card1 = new GrizzlyBears();
            Card card2 = new Forest();

            gd.addToExile(player1.getId(), card1);
            gd.addToExile(player1.getId(), card2);
            gd.exiledCardsWithSilverCounters.add(card1.getId());
            gd.exiledCardsWithSilverCounters.add(card2.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            // Should present a choice to the controller
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

            // Controller chooses Grizzly Bears
            harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

            // Grizzly Bears in hand
            harness.assertInHand(player1, "Grizzly Bears");
            // Forest still in exile with silver counter
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
            assertThat(gd.exiledCardsWithSilverCounters).contains(card2.getId());
            assertThat(gd.exiledCardsWithSilverCounters).doesNotContain(card1.getId());
        }

        @Test
        @DisplayName("−1 with no silver-counter cards does nothing")
        void minusOneWithNoSilverCounterCards() {
            Permanent karn = addReadyKarn(player1);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // Loyalty decreased
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("−1 ignores exiled cards without silver counters")
        void minusOneIgnoresNonSilverCards() {
            addReadyKarn(player1);
            Card noSilver = new GrizzlyBears();
            Card withSilver = new Forest();

            // noSilver is exiled but has no silver counter
            gd.addToExile(player1.getId(), noSilver);
            gd.addToExile(player1.getId(), withSilver);
            gd.exiledCardsWithSilverCounters.add(withSilver.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            // Only the silver-counter card (Forest) should be returned
            harness.assertInHand(player1, "Forest");
            // Grizzly Bears stays in exile
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    @Nested
    @DisplayName("−2 ability")
    @CardUsed({KarnScionOfUrza.class, ShortSword.class})
    class MinusTwoAbility {

        @Test
        @DisplayName("−2 creates a 0/0 Construct artifact creature token")
        void minusTwoCreatesConstructToken() {
            Permanent karn = addReadyKarn(player1);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2

            List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
            Permanent construct = bf.stream()
                    .filter(p -> p.getCard().getName().equals("Construct"))
                    .findFirst()
                    .orElse(null);
            assertThat(construct).isNotNull();
            assertThat(construct.getCard().isToken()).isTrue();
            assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(construct.getCard().getPower()).isEqualTo(0);
            assertThat(construct.getCard().getToughness()).isEqualTo(0);
        }

        @Test
        @DisplayName("Construct token gets +1/+1 for each artifact you control")
        void constructGetsBoostPerArtifact() {
            addReadyKarn(player1);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            Permanent construct = findPermanent(player1, "Construct");

            // The Construct itself is an artifact, so it counts itself
            var view = gqs.computeStaticBonus(gd, construct);
            // At minimum +1/+1 from itself being an artifact
            assertThat(view.power()).isGreaterThanOrEqualTo(1);
            assertThat(view.toughness()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Construct gets bigger with more artifacts on the battlefield")
        void constructScalesWithArtifacts() {
            addReadyKarn(player1);

            // Add some artifact permanents
            harness.addToBattlefield(player1, new ShortSword());
            harness.addToBattlefield(player1, new ShortSword());

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            Permanent construct = findPermanent(player1, "Construct");

            // Construct itself + 2 other artifacts = +3/+3
            var view = gqs.computeStaticBonus(gd, construct);
            assertThat(view.power()).isGreaterThanOrEqualTo(3);
            assertThat(view.toughness()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @DisplayName("+1 exiles with silver counter, then −1 returns it")
    void plusOneThenMinusOneIntegration() {
        Permanent karn = addReadyKarn(player1);
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));

        // +1: reveal Grizzly Bears and Forest, opponent chooses bears for hand
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        // Forest should be exiled with silver counter
        assertThat(gd.exiledCardsWithSilverCounters).contains(forest.getId());

        // Next turn: −1 to get Forest back
        karn.setLoyaltyActivationsThisTurn(0);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Forest should be in hand now
        harness.assertInHand(player1, "Forest");
        assertThat(gd.exiledCardsWithSilverCounters).doesNotContain(forest.getId());
    }

    @Test
    void minusOneDoesNotReturnOpponentsSilverCounterCard() {
        addReadyKarn(player1);
        Card forest = new Forest();
        gd.addToExile(player2.getId(), forest);
        gd.exiledCardsWithSilverCounters.add(forest.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anotherKarnCanReturnPreviouslyExiledSilverCounterCard() {
        addReadyKarn(player1);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        gd.playerBattlefields.get(player1.getId()).clear();
        addReadyKarn(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(forest);
    }

    @Test
    void constructCountsOnlyItsControllersArtifactsAndUpdatesAsTheyLeave() {
        addReadyKarn(player1);
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        harness.addToBattlefield(player2, new ShortSword());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent construct = findPermanent(player1, "Construct");

        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(sword);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
    }

    @Test
    @CardUsed({KarnScionOfUrza.class, Forest.class, PullFromEternity.class, SentinelTotem.class})
    void silverCounterDoesNotSurviveLeavingExileAndBeingExiledAgain() {
        Permanent karn = addReadyKarn(player1);
        Card forest = new Forest();
        Card otherForest = new Forest();
        harness.setLibrary(player1, List.of(forest, otherForest));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(otherForest.getId()));

        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, forest.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);

        harness.addToBattlefield(player1, new SentinelTotem());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest);

        karn.setLoyaltyActivationsThisTurn(0);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyKarn(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KarnScionOfUrza());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

}
