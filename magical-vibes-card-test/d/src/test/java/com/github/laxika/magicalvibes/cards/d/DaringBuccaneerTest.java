package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringBuccaneer.class, SailorOfMeans.class})
class DaringBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Pirate in hand it requires the additional {2}")
    void requiresAdditionalManaWithoutPirate() {
        harness.setHand(player1, List.of(new DaringBuccaneer()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {2} can be paid when no Pirate is revealed")
    void paysAdditionalManaWithoutPirate() {
        DaringBuccaneer buccaneer = new DaringBuccaneer();
        harness.setHand(player1, List.of(buccaneer));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daring Buccaneer");
    }

    @Test
    @DisplayName("A Pirate in hand lets it be cast without paying the additional {2}")
    void revealPirateAvoidsAdditionalMana() {
        DaringBuccaneer buccaneer = new DaringBuccaneer();
        SailorOfMeans pirateInHand = new SailorOfMeans();
        harness.setHand(player1, List.of(buccaneer, pirateInHand));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daring Buccaneer");
        harness.assertInHand(player1, "Sailor of Means");
    }

    @Test
    @DisplayName("The Pirate used to avoid paying {2} is publicly revealed during casting")
    void revealsPirateAsCastingCost() {
        harness.setHand(player1, List.of(new DaringBuccaneer(), new SailorOfMeans()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gameLogContains("reveals Sailor of Means")).isTrue();
        harness.assertInHand(player1, "Sailor of Means");
    }

    @Test
    @DisplayName("A Pirate in the opponent's hand cannot satisfy the additional cost")
    void opponentPirateDoesNotSatisfyCost() {
        harness.setHand(player1, List.of(new DaringBuccaneer()));
        harness.setHand(player2, List.of(new SailorOfMeans()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Pirate on the battlefield cannot satisfy the additional cost")
    void battlefieldPirateDoesNotSatisfyCost() {
        harness.setHand(player1, List.of(new DaringBuccaneer()));
        harness.addToBattlefield(player1, new SailorOfMeans());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another Daring Buccaneer can be revealed without leaving the hand")
    void anotherCopyCanSatisfyCost() {
        DaringBuccaneer revealed = new DaringBuccaneer();
        harness.setHand(player1, List.of(new DaringBuccaneer(), revealed));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daring Buccaneer");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }
}
