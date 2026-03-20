package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrandWarlordRadhaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two creatures triggers mana production — choosing RED adds 2 red mana")
    void attackWithTwoCreaturesAddsChosenColorMana() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(radha);

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declare both Radha and the bear as attackers
        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Resolve the attack trigger — should prompt for mana color choice
        harness.passBothPriorities();

        // Choose RED
        harness.handleListChoice(player1, "RED");

        // 2 attackers → 2 red mana
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing GREEN adds green mana equal to attacking creature count")
    void choosingGreenAddsGreenMana() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(radha);

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declare all 3 creatures as attackers
        gs.declareAttackers(gd, player1, List.of(0, 1, 2));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // 3 attackers → 3 green mana
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Radha does not need to attack herself — trigger fires when other creatures attack")
    void radhaDoesNotNeedToAttack() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(true); // Radha has summoning sickness, can't attack
        gd.playerBattlefields.get(player1.getId()).add(radha);

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only the bear attacks (index 1)
        gs.declareAttackers(gd, player1, List.of(1));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        // 1 attacker → 1 red mana
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Radha's mana does not drain at step transitions but other mana does")
    void persistentManaDoesNotDrainButOtherManaDoes() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(radha);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);

        // Add some non-persistent mana (e.g. from tapping a land)
        pool.add(ManaColor.RED, 2);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);

        // Advance step (calls drainManaPools internally) — persistent green stays, non-persistent red drains
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1); // Radha's mana survives
        assertThat(pool.get(ManaColor.RED)).isZero();        // Non-persistent mana drained
    }

    @Test
    @DisplayName("Persistent mana is cleared at end of turn and drains on next step transition")
    void persistentManaClearedAtEndOfTurn() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Simulate Radha's mana by adding persistent mana directly
        pool.addPersistentMana(ManaColor.RED, 3);

        // Advance step — persistent mana should survive
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);

        // Simulate end of turn by clearing persistent tracking
        pool.clearPersistentMana();

        // Now advance step — mana should drain
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Attacker count is locked at trigger time — removing attacker before resolution doesn't change mana amount")
    void attackerCountLockedAtTriggerTime() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(radha);

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);

        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declare 3 creatures as attackers
        gs.declareAttackers(gd, player1, List.of(0, 1, 2));

        // Remove one attacker from battlefield before trigger resolves (simulating kill spell)
        gd.playerBattlefields.get(player1.getId()).remove(2);

        // Resolve the attack trigger
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        // Still 3 mana — count was locked at trigger time (3 attackers were declared)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("No mana is produced when no creatures attack (trigger does not fire)")
    void noManaWhenNoCreaturesAttack() {
        Permanent radha = new Permanent(new GrandWarlordRadha());
        radha.setSummoningSick(true); // Can't attack
        gd.playerBattlefields.get(player1.getId()).add(radha);

        // No attack declared — just verify initial state
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // No mana should have been added
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }
}
