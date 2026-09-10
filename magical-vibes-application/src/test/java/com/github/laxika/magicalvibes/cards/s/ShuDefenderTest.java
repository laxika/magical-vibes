package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShuDefender.class, VolunteerMilitia.class, HighGround.class})
class ShuDefenderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent defender = addDefenderReady(player2);
        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(defender.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +0/+2 until end of turn")
    void blockTriggerGivesPlusZeroPlusTwo() {
        Permanent defender = addDefenderReady(player2);
        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(defender.getPowerModifier()).isEqualTo(0);
        assertThat(defender.getToughnessModifier()).isEqualTo(2);
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("+0/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent defender = addDefenderReady(player2);
        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(defender.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(defender.getPowerModifier()).isEqualTo(0);
        assertThat(defender.getToughnessModifier()).isEqualTo(0);
        assertThat(defender.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only once")
    void blockTriggerFiresOnlyOnceWhenBlockingMultipleCreatures() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent defender = addDefenderReady(player2);
        addAttackerReady(player1);
        addAttackerReady(player1);

        prepareDeclareBlockers();
        int defenderIndex = gd.playerBattlefields.get(player2.getId()).indexOf(defender);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderIndex, 0),
                new BlockerAssignment(defenderIndex, 1)));

        assertThat(gd.stack.stream()
                .filter(entry -> defender.getId().equals(entry.getSourcePermanentId())))
                .hasSize(1);
        resolveAllTriggers();

        assertThat(defender.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("No trigger fires when the creature is not blocking")
    void noTriggerWhenNotBlocking() {
        addDefenderReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addDefenderReady(Player player) {
        return addCreatureReady(player, new ShuDefender());
    }

    private void addAttackerReady(Player player) {
        Permanent attacker = addCreatureReady(player, new VolunteerMilitia());
        attacker.setAttacking(true);
    }
}
