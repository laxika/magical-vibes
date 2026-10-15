package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChocoboKick.class, Forest.class, GrizzlyBears.class, HillGiant.class, JayaVeneratedFiremage.class})
class ChocoboKickTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the controlled creature's power without kicker")
    void dealsPowerDamageWithoutKicker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Returns a land and deals twice the source power when kicked")
    void kicksByReturningLandAndDoublesDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(source.getId(), target.getId()), List.of(),
                false, land.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Requires an opposing creature as the second target")
    void requiresOpposingCreatureTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void kickedDamageIsExactlyTwicePower() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(source.getId(), target.getId()), List.of(),
                false, land.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void requiresAControlledCreatureAsTheFirstTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesPowerAtResolutionAndDoesNotDealDamageBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Chocobo Kick");
    }

    @Test
    void dealsNoDamageWhenOpponentTargetGainsHexproof() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        target.setCounterCount(CounterType.HEXPROOF, 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void kickerReturnsLandImmediatelyEvenWhenDamageCannotBeDealt() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(source.getId(), target.getId()), List.of(),
                false, land.getId(), null, null, null, null, true);
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.getPermanentRemovalService().removePermanentToHand(gd, source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void cannotReturnAnOpponentsLandForKicker() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(source.getId(), target.getId()), List.of(), false, land.getId(),
                null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void cannotReturnANonlandForKicker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(source.getId(), target.getId()), List.of(), false, source.getId(),
                null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("return cost");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void appliesJayasBonusToTheRedCreatureDealingDamage() {
        Permanent jaya = harness.addToBattlefieldAndReturn(player1, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new ChocoboKick()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(source.getMarkedDamage()).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
