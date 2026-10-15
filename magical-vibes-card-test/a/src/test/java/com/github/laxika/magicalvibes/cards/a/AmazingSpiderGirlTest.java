package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LuxurySuite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmazingSpiderGirl.class, GrizzlyBears.class, LuxurySuite.class})
class AmazingSpiderGirlTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {2}{W} by returning a tapped creature you control")
    void castsForWebSlingingCost() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Web-slinging returns the creature as a cost before the spell resolves")
    void returnsCreatureBeforeSpellResolves() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new AmazingSpiderGirl());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertInHand(player1, "Amazing Spider-Girl");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertInHand(player1, "Amazing Spider-Girl");
    }

    @Test
    @DisplayName("Web-slinging requires a tapped creature")
    void requiresTappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Can pay the normal mana cost without returning a creature")
    void castsNormallyWithoutCreature() {
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertNotInHand(player1, "Amazing Spider-Girl");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Web-slinging cannot return an opponent's creature")
    void cannotReturnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AmazingSpiderGirl());
        creature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found on your battlefield");

        harness.assertOnBattlefield(player2, "Amazing Spider-Girl");
        harness.assertInHand(player1, "Amazing Spider-Girl");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging returns a borrowed creature to its owner before resolution")
    void returnsBorrowedCreatureToOwner() {
        AmazingSpiderGirl borrowed = new AmazingSpiderGirl();
        borrowed.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, borrowed);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        creature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertNotInHand(player1, "Amazing Spider-Girl");
        harness.assertInHand(player2, "Amazing Spider-Girl");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        harness.assertInHand(player2, "Amazing Spider-Girl");
    }

    @Test
    @DisplayName("Web-slinging cannot return a tapped noncreature permanent")
    void cannotReturnTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LuxurySuite());
        land.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");

        harness.assertOnBattlefield(player1, "Luxury Suite");
        harness.assertInHand(player1, "Amazing Spider-Girl");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging cannot omit the return payment")
    void requiresReturnPayment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AmazingSpiderGirl());
        creature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging still requires white mana and does not return a creature on failure")
    void requiresWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AmazingSpiderGirl());
        creature.tap();
        harness.setHand(player1, List.of(new AmazingSpiderGirl()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Amazing Spider-Girl");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
