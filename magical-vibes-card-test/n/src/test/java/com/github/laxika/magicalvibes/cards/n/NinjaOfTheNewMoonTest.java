package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinjaOfTheNewMoon.class, GrizzlyBears.class})
class NinjaOfTheNewMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Ninja of the New Moon onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new NinjaOfTheNewMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Ninja of the New Moon");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Ninja of the New Moon");
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent ninja = findPermanent(player1, "Ninja of the New Moon");
        assertThat(ninja.isTapped()).isTrue();
        assertThat(ninja.isAttacking()).isTrue();
        assertThat(ninja.getAttackTarget()).isEqualTo(player2.getId());
        harness.setLife(player2, 20);
        resolveCombat();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheNewMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    @Test
    @DisplayName("Ninjutsu cannot be activated before blockers are declared")
    void ninjutsuRejectsAttackerBeforeBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.setHand(player1, List.of(new NinjaOfTheNewMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Ninja of the New Moon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient ninjutsu mana does not return the attacker")
    void insufficientManaLeavesAttackerOnBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new NinjaOfTheNewMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Ninja of the New Moon");
        assertThat(gd.stack).isEmpty();
    }
}
