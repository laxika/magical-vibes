package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuergarMineCaptain.class})
class DuergarMineCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping an attacking captain boosts itself and other attackers")
    void boostsAllAttackingCreatures() {
        Permanent captain = addTapped(player1, new DuergarMineCaptain());
        captain.setAttacking(true);
        Permanent ownAttacker = addCreatureReady(player1, new DuergarMineCaptain());
        ownAttacker.setAttacking(true);
        Permanent opponentBystander = addCreatureReady(player2, new DuergarMineCaptain());

        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentBystander.getEffectivePower()).isEqualTo(2);
        assertThat(captain.getEffectivePower()).isEqualTo(3);
        // Paying {Q} untapped the captain.
        assertThat(captain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-attacking creatures are unaffected")
    void nonAttackingUnaffected() {
        addTapped(player1, new DuergarMineCaptain());
        Permanent bystander = addCreatureReady(player1, new DuergarMineCaptain());

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
        Permanent attacker = addCreatureReady(player1, new DuergarMineCaptain());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the captain is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new DuergarMineCaptain());
        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("A defending captain can pay white mana to boost opposing attackers")
    void boostsOpposingAttackersWithWhiteMana() {
        Permanent captain = addTapped(player2, new DuergarMineCaptain());
        Permanent attacker = addCreatureReady(player1, new DuergarMineCaptain());
        attacker.setAttacking(true);
        harness.addMana(player2, ManaColor.WHITE, 2);
        enterCombatWithPriority(player1);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        assertThat(captain.isTapped()).isFalse();
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(captain.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent captain = addTapped(player1, new DuergarMineCaptain());
        captain.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(captain.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost selects attackers on resolution and persists after they stop attacking")
    void selectsAttackersOnResolution() {
        addTapped(player1, new DuergarMineCaptain());
        Permanent attacker = addCreatureReady(player1, new DuergarMineCaptain());
        attacker.setAttacking(true);
        Permanent removedAttacker = addCreatureReady(player1, new DuergarMineCaptain());
        removedAttacker.setAttacking(true);
        harness.addMana(player1, ManaColor.RED, 2);
        enterCombatWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        removedAttacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(removedAttacker.getEffectivePower()).isEqualTo(2);
        attacker.setAttacking(false);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterCombatWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }
}
