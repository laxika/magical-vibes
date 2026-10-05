package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.StreetWraith;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NewPerspectives.class, Censor.class, Colossapede.class, Forest.class,
        NagaVitalist.class, SongOfTheDryads.class, StreetWraith.class})
class NewPerspectivesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws three cards")
    void entersDrawsThreeCards() {
        harness.setHand(player1, List.of(new NewPerspectives()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setLibrary(player1, List.of(new Forest(), new Colossapede(), new NagaVitalist()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "New Perspectives");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Colossapede");
        harness.assertInHand(player1, "Naga Vitalist");
    }

    @Test
    @DisplayName("Cycling costs {0} with seven or more cards in hand")
    void cyclingIsFreeWithSevenCardsInHand() {
        harness.addToBattlefield(player1, new NewPerspectives());
        harness.setHand(player1, sevenCardHandWithCensor());
        harness.setLibrary(player1, List.of(new Colossapede()));
        // No mana in the pool — the {U} cycling cost must be replaced with {0}.

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Colossapede");
    }

    @Test
    @DisplayName("Cycling still costs mana with fewer than seven cards in hand")
    void cyclingCostsManaWithFewerThanSevenCards() {
        harness.addToBattlefield(player1, new NewPerspectives());
        List<Card> hand = new ArrayList<>();
        hand.add(new Censor());
        for (int i = 0; i < 5; i++) { // six cards total — below the seven threshold
            hand.add(new NagaVitalist());
        }
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Colossapede()));
        // No mana in the pool: the replacement does not apply, so {U} is unaffordable.

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Censor");
    }

    @Test
    @DisplayName("Cycling still costs mana without New Perspectives on the battlefield")
    void cyclingCostsManaWithoutNewPerspectives() {
        harness.setHand(player1, sevenCardHandWithCensor());
        harness.setLibrary(player1, List.of(new Colossapede()));
        // Seven cards in hand but no New Perspectives, no mana: {U} is unaffordable.

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Censor");
    }

    @Test
    @DisplayName("An opponent's New Perspectives does not grant free cycling")
    void opponentDoesNotGrantFreeCycling() {
        harness.addToBattlefield(player2, new NewPerspectives());
        harness.setHand(player1, sevenCardHandWithCensor());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling below the threshold succeeds when its normal cost is paid")
    void paidCyclingBelowThreshold() {
        harness.addToBattlefield(player1, new NewPerspectives());
        List<Card> hand = sevenCardHandWithCensor();
        hand.removeLast();
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Colossapede");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Eligibility is checked again before each cycling activation")
    void handSizeIsCheckedForEachActivation() {
        harness.addToBattlefield(player1, new NewPerspectives());
        List<Card> hand = sevenCardHandWithCensor();
        hand.set(1, new Censor());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Forest(), new Colossapede()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertInGraveyard(player1, "Censor");
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Censor");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Censor).hasSize(2);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Colossapede");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("New Perspectives loses its free cycling ability when turned into a Forest")
    void becomingForestRemovesFreeCycling() {
        var perspectives = harness.addToBattlefieldAndReturn(player1, new NewPerspectives());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, perspectives.getId());
        harness.passBothPriorities();
        harness.setHand(player1, sevenCardHandWithCensor());

        assertThat(gqs.isLand(gd, perspectives)).isTrue();
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
    }

    @Test
    @DisplayName("The zero cycling cost also replaces a cycling life payment")
    void freeCyclingReplacesLifeCost() {
        harness.addToBattlefield(player1, new NewPerspectives());
        List<Card> hand = sevenCardHandWithCensor();
        hand.set(0, new StreetWraith());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new Colossapede()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Street Wraith");
        harness.assertInHand(player1, "Colossapede");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    private List<Card> sevenCardHandWithCensor() {
        List<Card> hand = new ArrayList<>();
        hand.add(new Censor()); // index 0, the card to cycle
        for (int i = 0; i < 6; i++) {
            hand.add(new NagaVitalist());
        }
        return hand;
    }
}
