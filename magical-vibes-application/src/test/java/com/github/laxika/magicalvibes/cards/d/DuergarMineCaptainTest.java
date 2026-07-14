package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuergarMineCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping the captain gives all attacking creatures +1/+0, both sides")
    void boostsAllAttackingCreatures() {
        Permanent captain = addTapped(player1, new DuergarMineCaptain());
        Permanent ownAttacker = addReady(player1, new GrizzlyBears());   // 2/2
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addReady(player2, new GrizzlyBears()); // 2/2
        opponentAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(3);
        // Paying {Q} untapped the captain.
        assertThat(captain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-attacking creatures are unaffected")
    void nonAttackingUnaffected() {
        addTapped(player1, new DuergarMineCaptain());
        Permanent bystander = addReady(player1, new GrizzlyBears()); // 2/2, not attacking

        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bystander.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        addTapped(player1, new DuergarMineCaptain());
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the captain is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new DuergarMineCaptain());
        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterCombatWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }
}
