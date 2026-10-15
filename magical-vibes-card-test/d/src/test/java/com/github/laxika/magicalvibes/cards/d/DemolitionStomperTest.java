package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemolitionStomper.class, GrizzlyBears.class, HillGiant.class})
class DemolitionStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 5 animates Demolition Stomper and taps creatures with total power 5")
    void crewAnimatesStomper() {
        Permanent stomper = addStomperReady(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(stomper.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, stomper)).isTrue();
        assertThat(giant.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Demolition Stomper cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        addAttackingStomper();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Demolition Stomper can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        addAttackingStomper();
        Permanent giant = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    private Permanent addStomperReady(Player player) {
        return addCreatureReady(player, new DemolitionStomper());
    }

    @Test
    void crewIsPaidBeforeAnimationResolvesAndEndsAtCleanup() {
        Permanent stomper = addStomperReady(player1);
        Permanent first = addCreatureReady(player1, new HillGiant());
        Permanent second = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, stomper)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, stomper)).isTrue();
        assertThat(stomper.isTapped()).isFalse();

        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, stomper)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, stomper)).isFalse();
    }

    @Test
    void summoningSickCreaturesCanCrew() {
        Permanent stomper = harness.addToBattlefieldAndReturn(player1, new DemolitionStomper());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        stomper.setSummoningSick(true);
        giant.setSummoningSick(true);
        bears.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, stomper)).isTrue();
        assertThat(stomper.isSummoningSick()).isTrue();
        assertThat(giant.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void fourPowerCannotPayCrewFive() {
        Permanent stomper = addStomperReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, stomper)).isFalse();
    }

    @Test
    void tappedAndOpposingCreaturesCannotContributeToCrew() {
        addStomperReady(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent tappedBears = addCreatureReady(player1, new GrizzlyBears());
        tappedBears.tap();
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(giant.isTapped()).isFalse();
        assertThat(opposingBears.isTapped()).isFalse();
    }

    @Test
    void boostedTwoPowerCreatureCanBlock() {
        addAttackingStomper();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setPowerModifier(1);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    void weakenedThreePowerCreatureCannotBlock() {
        addAttackingStomper();
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.setPowerModifier(-1);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    private Permanent addAttackingStomper() {
        Permanent stomper = addStomperReady(player1);
        stomper.setAnimatedUntilEndOfTurn(true);
        stomper.setAnimatedPower(10);
        stomper.setAnimatedToughness(7);
        stomper.setAttacking(true);
        return stomper;
    }
}
