package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deluge.class, AirElemental.class, Forest.class, GrizzlyBears.class})
class DelugeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Deluge(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving taps all creatures without flying on both sides")
    void tapsAllCreaturesWithoutFlying() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap noncreature permanents")
    void doesNotTapNoncreaturePermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap creatures with flying")
    void doesNotTapCreaturesWithFlying() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(airElemental.isTapped()).isFalse();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deluge goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deluge");
    }

    @Test
    @DisplayName("Does not tap creatures with flying on opponent's side either")
    void doesNotTapOpponentFlyingCreatures() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Deluge(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(airElemental.isTapped()).isFalse();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }
}

