package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
