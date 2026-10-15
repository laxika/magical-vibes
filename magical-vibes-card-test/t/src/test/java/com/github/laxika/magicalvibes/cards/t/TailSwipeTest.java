package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TailSwipe.class, GrizzlyBears.class})
class TailSwipeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creature before it fights when cast during your main phase")
    void boostsDuringMainPhaseBeforeFight() {
        Permanent myBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, List.of(
                myBear.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        assertThat(myBear.getPowerModifier()).isEqualTo(1);
        assertThat(myBear.getToughnessModifier()).isEqualTo(1);
        assertThat(myBear.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fights without the bonus when cast outside your main phase")
    void doesNotBoostOutsideMainPhase() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires a creature you control as the first target")
    void rejectsOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player1, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Requires a creature you do not control as the second target")
    void rejectsOwnCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(
                battlefield.get(0).getId(), battlefield.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    @DisplayName("Does not boost during the opponent's main phase")
    void doesNotBoostDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Boosts during your postcombat main phase")
    void boostsDuringPostcombatMainPhase() {
        Permanent myBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, List.of(
                myBear.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(myBear.getPowerModifier()).isEqualTo(1);
        assertThat(myBear.getToughnessModifier()).isEqualTo(1);
        assertThat(myBear.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(myBear.getPowerModifier()).isZero();
        assertThat(myBear.getToughnessModifier()).isZero();
        assertThat(myBear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still boosts your creature but does not fight when the other target leaves")
    void boostsWithoutFightWhenOpponentTargetLeaves() {
        Permanent myBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, List.of(myBear.getId(), otherBear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(otherBear);
        harness.passBothPriorities();

        assertThat(myBear.getPowerModifier()).isEqualTo(1);
        assertThat(myBear.getToughnessModifier()).isEqualTo(1);
        assertThat(myBear.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tail Swipe");
    }

    @Test
    @DisplayName("Does not boost or fight the opponent's creature when your target leaves")
    void doesNotAffectOpponentWhenOwnTargetLeaves() {
        Permanent myBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, List.of(myBear.getId(), otherBear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(myBear);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(otherBear.getPowerModifier()).isZero();
        assertThat(otherBear.getToughnessModifier()).isZero();
        assertThat(otherBear.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tail Swipe");
    }
}
