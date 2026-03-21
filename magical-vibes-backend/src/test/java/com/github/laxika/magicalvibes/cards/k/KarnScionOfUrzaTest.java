package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionReturnSilverCounterCardEffect;
import com.github.laxika.magicalvibes.model.effect.KarnScionRevealTwoOpponentChoosesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KarnScionOfUrzaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three activated abilities with correct loyalty costs and effects")
    void hasThreeAbilities() {
        KarnScionOfUrza card = new KarnScionOfUrza();

        assertThat(card.getActivatedAbilities()).hasSize(3);

        var plus1 = card.getActivatedAbilities().get(0);
        assertThat(plus1.getLoyaltyCost()).isEqualTo(1);
        assertThat(plus1.getEffects().getFirst()).isInstanceOf(KarnScionRevealTwoOpponentChoosesEffect.class);

        var minus1 = card.getActivatedAbilities().get(1);
        assertThat(minus1.getLoyaltyCost()).isEqualTo(-1);
        assertThat(minus1.getEffects().getFirst()).isInstanceOf(KarnScionReturnSilverCounterCardEffect.class);

        var minus2 = card.getActivatedAbilities().get(2);
        assertThat(minus2.getLoyaltyCost()).isEqualTo(-2);
        assertThat(minus2.getEffects().getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Karn, Scion of Urza");
    }

    @Test
    @DisplayName("Resolving puts Karn on battlefield with loyalty 5")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Karn, Scion of Urza"));
        Permanent karn = findPermanent(player1, "Karn, Scion of Urza");
        assertThat(karn.getLoyaltyCounters()).isEqualTo(5);
        assertThat(karn.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: Reveal two, opponent chooses =====

    @Nested
    @DisplayName("+1 ability")
    class PlusOneAbility {

        @Test
        @DisplayName("+1 increases loyalty and presents opponent with choice")
        void plusOneIncreasesLoyaltyAndPresentsChoice() {
            Permanent karn = addReadyKarn(player1);
            // Put known cards on top of library
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new Forest()));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(karn.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
        }

        @Test
        @DisplayName("Opponent chooses a card — chosen goes to controller's hand, other exiled with silver counter")
        void opponentChoosesCardForHand() {
            Permanent karn = addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Opponent (player2) chooses Grizzly Bears for the controller's hand
            harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId()));

            // Grizzly Bears should be in controller's hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Forest should be exiled with a silver counter
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
            assertThat(gd.exiledCardsWithSilverCounters).contains(forest.getId());
        }

        @Test
        @DisplayName("Opponent can choose the other card instead")
        void opponentChoosesOtherCard() {
            Permanent karn = addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Opponent chooses Forest for the controller's hand
            harness.handleMultipleGraveyardCardsChosen(player2, List.of(forest.getId()));

            // Forest in controller's hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));

            // Grizzly Bears exiled with silver counter
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exiledCardsWithSilverCounters).contains(bears.getId());
        }

        @Test
        @DisplayName("+1 with only one card in library puts it into hand")
        void plusOneWithOneCardInLibrary() {
            Permanent karn = addReadyKarn(player1);
            Card bears = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).add(bears);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            // Only one card — goes directly to hand, no opponent choice
            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("+1 with empty library does nothing")
        void plusOneWithEmptyLibrary() {
            Permanent karn = addReadyKarn(player1);
            gd.playerDecks.get(player1.getId()).clear();

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(karn.getLoyaltyCounters()).isEqualTo(6); // Loyalty still increased
        }
    }

    // ===== −1 ability: Return silver counter card from exile =====

    @Nested
    @DisplayName("−1 ability")
    class MinusOneAbility {

        @Test
        @DisplayName("−1 returns a silver-countered card from exile to hand")
        void minusOneReturnsSilverCounterCard() {
            Permanent karn = addReadyKarn(player1);
            Card exiledCard = new GrizzlyBears();

            // Set up exiled card with silver counter
            gd.playerExiledCards.computeIfAbsent(player1.getId(), k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                    .add(exiledCard);
            gd.exiledCardsWithSilverCounters.add(exiledCard.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(karn.getLoyaltyCounters()).isEqualTo(4); // 5 - 1
            // Card should be in hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            // Card should no longer be in exile
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            // Silver counter tracking should be removed
            assertThat(gd.exiledCardsWithSilverCounters).doesNotContain(exiledCard.getId());
        }

        @Test
        @DisplayName("−1 with multiple silver-counter cards presents choice")
        void minusOneWithMultipleCardsPresentsChoice() {
            Permanent karn = addReadyKarn(player1);
            Card card1 = new GrizzlyBears();
            Card card2 = new Forest();

            gd.playerExiledCards.computeIfAbsent(player1.getId(), k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                    .addAll(List.of(card1, card2));
            gd.exiledCardsWithSilverCounters.add(card1.getId());
            gd.exiledCardsWithSilverCounters.add(card2.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            // Should present a choice to the controller
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

            // Controller chooses Grizzly Bears
            harness.handleMultipleGraveyardCardsChosen(player1, List.of(card1.getId()));

            // Grizzly Bears in hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            // Forest still in exile with silver counter
            assertThat(gd.playerExiledCards.get(player1.getId()))
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

            assertThat(karn.getLoyaltyCounters()).isEqualTo(4); // Loyalty decreased
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("−1 ignores exiled cards without silver counters")
        void minusOneIgnoresNonSilverCards() {
            Permanent karn = addReadyKarn(player1);
            Card noSilver = new GrizzlyBears();
            Card withSilver = new Forest();

            // noSilver is exiled but has no silver counter
            gd.playerExiledCards.computeIfAbsent(player1.getId(), k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                    .addAll(List.of(noSilver, withSilver));
            gd.exiledCardsWithSilverCounters.add(withSilver.getId());

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            // Only the silver-counter card (Forest) should be returned
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
            // Grizzly Bears stays in exile
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // ===== −2 ability: Create Construct token =====

    @Nested
    @DisplayName("−2 ability")
    class MinusTwoAbility {

        @Test
        @DisplayName("−2 creates a 0/0 Construct artifact creature token")
        void minusTwoCreatesConstructToken() {
            Permanent karn = addReadyKarn(player1);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(karn.getLoyaltyCounters()).isEqualTo(3); // 5 - 2

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
            Permanent karn = addReadyKarn(player1);

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
            Permanent karn = addReadyKarn(player1);

            // Add some artifact permanents
            harness.addToBattlefield(player1, createArtifactToken());
            harness.addToBattlefield(player1, createArtifactToken());

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            Permanent construct = findPermanent(player1, "Construct");

            // Construct itself + 2 other artifacts = +3/+3
            var view = gqs.computeStaticBonus(gd, construct);
            assertThat(view.power()).isGreaterThanOrEqualTo(3);
            assertThat(view.toughness()).isGreaterThanOrEqualTo(3);
        }
    }

    // ===== +1 and −1 integration =====

    @Test
    @DisplayName("+1 exiles with silver counter, then −1 returns it")
    void plusOneThenMinusOneIntegration() {
        Permanent karn = addReadyKarn(player1);
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest));

        // +1: reveal Grizzly Bears and Forest, opponent chooses bears for hand
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(bears.getId()));

        // Forest should be exiled with silver counter
        assertThat(gd.exiledCardsWithSilverCounters).contains(forest.getId());

        // Next turn: −1 to get Forest back
        karn.setLoyaltyActivationsThisTurn(0);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Forest should be in hand now
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.exiledCardsWithSilverCounters).doesNotContain(forest.getId());
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name)).findFirst().orElseThrow();
    }

    private Permanent addReadyKarn(Player player) {
        KarnScionOfUrza card = new KarnScionOfUrza();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(5);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Card createArtifactToken() {
        Card token = new Card() {};
        token.setName("Test Artifact");
        token.setToken(true);
        token.setType(CardType.ARTIFACT);
        return token;
    }
}
