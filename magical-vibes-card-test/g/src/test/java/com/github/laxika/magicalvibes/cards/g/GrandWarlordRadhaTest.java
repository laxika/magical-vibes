package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandWarlordRadha.class, LlanowarElves.class})
class GrandWarlordRadhaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two creatures triggers mana production — choosing RED adds 2 red mana")
    void attackWithTwoCreaturesAddsChosenColorMana() {
        addCreatureReady(player1, new GrandWarlordRadha());

        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0, 1));

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
        addCreatureReady(player1, new GrandWarlordRadha());

        addCreatureReady(player1, new LlanowarElves());

        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0, 1, 2));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // 3 attackers → 3 green mana
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Radha does not need to attack herself — trigger fires when other creatures attack")
    void radhaDoesNotNeedToAttack() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new GrandWarlordRadha());
        radha.setSummoningSick(true); // Radha stays back even though haste allows her to attack

        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(1));

        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        // 1 attacker → 1 red mana
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Radha's mana does not drain at step transitions but other mana does")
    void persistentManaDoesNotDrainButOtherManaDoes() {
        addCreatureReady(player1, new GrandWarlordRadha());

        declareAttackers(List.of(0));

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
    @DisplayName("Radha's unspent mana expires when the turn ends")
    void persistentManaClearedAtEndOfTurn() {
        addCreatureReady(player1, new GrandWarlordRadha());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Two attackers can produce one red and one green mana")
    void attackingCreaturesCanProduceMixedMana() {
        addCreatureReady(player1, new GrandWarlordRadha());
        addCreatureReady(player1, new LlanowarElves());
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Radha can attack immediately and produce mana")
    void hasteAllowsImmediateAttack() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new GrandWarlordRadha());
        radha.setSummoningSick(true);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Radha's trigger resolves after Radha leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        addCreatureReady(player1, new GrandWarlordRadha());
        addCreatureReady(player1, new LlanowarElves());
        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering attacking do not increase Radha's mana")
    void enteringAttackingDoesNotIncreaseMana() {
        addCreatureReady(player1, new GrandWarlordRadha());
        declareAttackers(List.of(0));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.setAttacking(true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents' attacking creatures do not trigger Radha")
    void opponentsAttackDoesNotProduceMana() {
        harness.addToBattlefield(player1, new GrandWarlordRadha());
        addCreatureReady(player2, new LlanowarElves());
        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacker count is locked at trigger time — removing attacker before resolution doesn't change mana amount")
    void attackerCountLockedAtTriggerTime() {
        addCreatureReady(player1, new GrandWarlordRadha());

        addCreatureReady(player1, new LlanowarElves());

        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0, 1, 2));

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
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new GrandWarlordRadha());
        radha.setSummoningSick(true);

        // No attack declared — just verify initial state
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // No mana should have been added
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }
}
