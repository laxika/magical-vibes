package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavenEarth.class, SerraAngel.class, GrizzlyBears.class})
class HeavenEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Heaven deals X damage to creatures with flying")
    void heavenDamagesFliers() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4 flyer
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2 ground
        harness.setHand(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(serra.getMarkedDamage()).isEqualTo(3);
        assertThat(bear.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Heaven");
    }

    @Test
    @DisplayName("Heaven with X=0 deals no damage")
    void heavenXZero() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(serra.getMarkedDamage()).isEqualTo(0);
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Earth from graveyard damages non-fliers then exiles")
    void earthDamagesNonFliersThenExiles() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, 2, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(2);
        assertThat(serra.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Heaven") || c.getName().equals("Earth"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Heaven"));
    }

    @Test
    @DisplayName("Earth with X=0 deals no damage then exiles")
    void earthXZeroExiles() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Heaven"));
    }

    @Test
    @DisplayName("Heaven kills fliers on both boards without damaging ground creatures or players")
    void heavenSweepsBothBoards() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(opposingBear.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Earth can follow Heaven and kills ground creatures on both boards before exiling the card")
    void earthFollowsHeavenAndSweepsBothBoards() {
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Heaven");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, 2, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ownAngel.getMarkedDamage()).isZero();
        assertThat(opposingAngel.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Heaven");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Heaven"));
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Earth cannot be cast during combat even though Heaven is an instant")
    void earthRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new HeavenEarth()));
        harness.addMana(player1, ManaColor.RED, 2);
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Heaven");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
