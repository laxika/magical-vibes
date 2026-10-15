package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortressCyclops.class, ScabClanCharger.class, BraveTheSands.class})
class FortressCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Fortress Cyclops +3/+0 until end of turn")
    void attackTriggerBoostsPower() {
        Permanent cyclops = addCreatureReady(player1, new FortressCyclops());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cyclops.getPowerModifier()).isEqualTo(3);
        assertThat(cyclops.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOff() {
        Permanent cyclops = addCreatureReady(player1, new FortressCyclops());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities(); // END_STEP -> CLEANUP

        assertThat(cyclops.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking gives Fortress Cyclops +0/+3 until end of turn")
    void blockTriggerBoostsToughness() {
        Permanent attacker = addCreatureReady(player1, new ScabClanCharger());
        attacker.setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(6);
    }

    @Test
    @DisplayName("No boost when Fortress Cyclops neither attacks nor blocks")
    void noBoostWithoutCombat() {
        Permanent attacker = addCreatureReady(player1, new ScabClanCharger());
        attacker.setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack bonus applies only after the trigger resolves")
    void attackBonusWaitsForResolution() {
        Permanent cyclops = addCreatureReady(player1, new FortressCyclops());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(cyclops.getPowerModifier()).isZero();
        resolveAllTriggers();
        assertThat(cyclops.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The block boost lasts through end of combat and expires at cleanup")
    void blockBoostLastsUntilEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new ScabClanCharger());
        attacker.setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThat(cyclops.getToughnessModifier()).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(cyclops.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking two creatures triggers the toughness bonus only once")
    void blockingMultipleCreaturesTriggersOnce() {
        addCreatureReady(player1, new ScabClanCharger()).setAttacking(true);
        addCreatureReady(player1, new ScabClanCharger()).setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());
        harness.addToBattlefield(player2, new BraveTheSands());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(6);
    }
}
