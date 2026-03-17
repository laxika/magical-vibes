package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HamletCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Hamlet Captain boosts other Humans +1/+1")
    void attackBoostsOtherHumans() {
        // Hamlet Captain (Human Warrior) at index 0
        Permanent captain = new Permanent(new HamletCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        // Elite Vanguard (Human Soldier) at index 1
        Permanent human = new Permanent(new EliteVanguard());
        human.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(human);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the trigger
        harness.passBothPriorities();

        // Elite Vanguard (2/1) should now be 3/2
        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Hamlet Captain does not boost itself")
    void attackDoesNotBoostSelf() {
        Permanent captain = new Permanent(new HamletCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Hamlet Captain should NOT get the boost (it says "other Humans")
        assertThat(captain.getPowerModifier()).isEqualTo(0);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking with Hamlet Captain does not boost non-Human creatures")
    void attackDoesNotBoostNonHumans() {
        Permanent captain = new Permanent(new HamletCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        // Grizzly Bears is a Bear, not a Human
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Grizzly Bears should not get the boost
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Blocking with Hamlet Captain boosts other Humans +1/+1")
    void blockBoostsOtherHumans() {
        // Hamlet Captain on player2's side (blocker)
        Permanent captain = new Permanent(new HamletCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(captain);

        // Another Human on player2's side
        Permanent human = new Permanent(new EliteVanguard());
        human.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(human);

        // Attacker on player1's side
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Hamlet Captain (index 0 on player2's battlefield) blocks attacker (index 0 on player1's battlefield)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Elite Vanguard should get +1/+1
        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
    }
}
