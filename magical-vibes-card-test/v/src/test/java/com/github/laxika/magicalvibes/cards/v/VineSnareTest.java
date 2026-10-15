package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VineSnare.class, GrizzlyBears.class, AirElemental.class, AvatarOfMight.class})
class VineSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 4 or less are prevented from dealing combat damage")
    void preventsSmallCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, elemental, true)).isTrue();
        harness.assertInGraveyard(player1, "Vine Snare");
    }

    @Test
    @DisplayName("Noncombat damage from small creatures is unaffected")
    void doesNotPreventNoncombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }

    @Test
    @DisplayName("Creatures with power 5 or greater still deal combat damage")
    void exemptsBigCreatures() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, avatar, true)).isFalse();
    }

    @Test
    @DisplayName("Small attackers and blockers deal no combat damage to each other")
    void preventsDamageFromBothSidesOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        castVineSnare();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only the large unblocked attacker deals combat damage to the player")
    void preventsSmallAttackerDamageToPlayer() {
        addCreatureReady(player2, new AirElemental());
        addCreatureReady(player2, new AvatarOfMight());
        harness.setLife(player1, 20);

        castVineSnare();

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("A creature that grows from four to five power after resolution deals damage")
    void usesIncreasedPowerAtDamageTime() {
        Permanent attacker = addCreatureReady(player2, new AirElemental());
        harness.setLife(player1, 20);

        castVineSnare();
        attacker.setPowerModifier(1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("A creature that shrinks to four power after resolution deals no combat damage")
    void usesDecreasedPowerAtDamageTime() {
        Permanent attacker = addCreatureReady(player2, new AvatarOfMight());
        harness.setLife(player1, 20);

        castVineSnare();
        attacker.setPowerModifier(-4);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Small creatures entering after resolution also deal no combat damage")
    void appliesToCreaturesEnteringLater() {
        castVineSnare();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Small creatures deal combat damage again on the next turn")
    void expiresAtEndOfTurn() {
        castVineSnare();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private void castVineSnare() {
        harness.setHand(player1, List.of(new VineSnare()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
    }
}
