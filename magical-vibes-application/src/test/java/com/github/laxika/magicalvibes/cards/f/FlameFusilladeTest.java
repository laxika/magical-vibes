package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameFusillade.class, Forest.class, GrizzlyBears.class})
class FlameFusilladeTest extends BaseCardTest {

    @Test
    @DisplayName("Permanents you control gain the tap damage ability until end of turn")
    void permanentsGainTapDamageAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castFlameFusillade();

        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permanents that enter after Flame Fusillade resolves do not gain the ability")
    void laterPermanentsDoNotGainAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        castFlameFusillade();

        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        castFlameFusillade();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with summoning sickness cannot activate the granted tap ability")
    void summoningSickCreatureCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFlameFusillade();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A land can activate again after untapping, but not while tapped")
    void tapCostMustBePaidForEachActivation() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castFlameFusillade();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(forest.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 19);

        forest.setTapped(false);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A land entering while the spell is on the stack gains the ability on resolution")
    void permanentEnteringBeforeResolutionGainsAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FlameFusillade(), "{3}{R}");
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An activated ability still resolves after its source creature dies")
    void activatedAbilitySurvivesSourceDying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castFlameFusillade();

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
    private void castFlameFusillade() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FlameFusillade(), "{3}{R}");
        harness.passBothPriorities();
    }
}
