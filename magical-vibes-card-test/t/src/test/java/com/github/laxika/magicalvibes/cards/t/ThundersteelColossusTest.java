package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThundersteelColossus.class, JukaiTrainee.class, NetworkDisruptor.class, BambooGroveArcher.class})
class ThundersteelColossusTest extends BaseCardTest {

    @Test
    void crewAnimatesColossusAndTapsCrew() {
        Permanent colossus = addCreatureReady(player1, new ThundersteelColossus());
        Permanent crew = addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationEndsAtEndOfTurn() {
        Permanent colossus = addCreatureReady(player1, new ThundersteelColossus());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, colossus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isFalse();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new ThundersteelColossus());
        addCreatureReady(player1, new NetworkDisruptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewCostIsPaidBeforeAnimationResolves() {
        Permanent colossus = addCreatureReady(player1, new ThundersteelColossus());
        Permanent crew = addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(colossus.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, colossus)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, colossus)).isTrue();
    }

    @Test
    void twoSummoningSickOnePowerCreaturesCanCrew() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ThundersteelColossus());
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new NetworkDisruptor());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new NetworkDisruptor());
        firstCrew.setSummoningSick(true);
        secondCrew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    void cannotCrewWithTappedCreature() {
        addCreatureReady(player1, new ThundersteelColossus());
        Permanent crew = addCreatureReady(player1, new JukaiTrainee());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void cannotCrewWithOpponentsCreature() {
        addCreatureReady(player1, new ThundersteelColossus());
        Permanent crew = addCreatureReady(player2, new JukaiTrainee());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    void animatedColossusCannotCrewItself() {
        Permanent colossus = addCreatureReady(player1, new ThundersteelColossus());
        addCreatureReady(player1, new JukaiTrainee());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    void newlyEnteredColossusCanAttackAfterCrewing() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ThundersteelColossus());
        colossus.setSummoningSick(true);
        addCreatureReady(player1, new JukaiTrainee());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 13);
        assertThat(colossus.isTapped()).isTrue();
    }

    @Test
    void crewedColossusTramplesOverBlocker() {
        addCreatureReady(player1, new ThundersteelColossus());
        addCreatureReady(player1, new JukaiTrainee());
        Permanent blocker = addCreatureReady(player2, new BambooGroveArcher());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Bamboo Grove Archer");
        harness.assertOnBattlefield(player1, "Thundersteel Colossus");
    }
}
