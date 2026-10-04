package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disembowel;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HammerfistGiant.class, Watchwolf.class, SnappingDrake.class, Disembowel.class})
class HammerfistGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it deals 4 damage to each player and each creature without flying")
    void tappingItDamagesPlayersAndGroundCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HammerfistGiant());
        harness.addToBattlefield(player2, new Watchwolf());
        Permanent snappingDrake = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hammerfist Giant");
        harness.assertInGraveyard(player1, "Hammerfist Giant");
        harness.assertNotOnBattlefield(player2, "Watchwolf");
        assertThat(snappingDrake.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Snapping Drake");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Its ability taps the Giant")
    void activationTapsTheGiant() {
        Permanent giant = addCreatureReady(player1, new HammerfistGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new HammerfistGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new HammerfistGiant());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void damagesGroundCreaturesAndSparesFlyingCreaturesOnBothSides() {
        addCreatureReady(player1, new HammerfistGiant());
        harness.addToBattlefield(player1, new Watchwolf());
        harness.addToBattlefield(player2, new Watchwolf());
        Permanent friendlyDrake = harness.addToBattlefieldAndReturn(player1, new SnappingDrake());
        Permanent opposingDrake = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertOnBattlefield(player1, "Snapping Drake");
        harness.assertOnBattlefield(player2, "Snapping Drake");
        assertThat(friendlyDrake.getMarkedDamage()).isZero();
        assertThat(opposingDrake.getMarkedDamage()).isZero();
    }

    @Test
    void abilityStillResolvesAfterGiantIsDestroyedInResponse() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player1, new HammerfistGiant());
        harness.addToBattlefield(player2, new Watchwolf());
        harness.addToBattlefield(player2, new SnappingDrake());
        harness.setHand(player2, List.of(new Disembowel()));
        harness.addMana(player2, ManaColor.BLACK, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, 6, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hammerfist Giant");
        harness.assertOnBattlefield(player2, "Watchwolf");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertOnBattlefield(player2, "Snapping Drake");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }
}
