package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngusMackenzie.class, BarbaryApes.class, PsionicEntity.class})
class AngusMackenzieTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the colored activation cost, taps Angus, and prevents combat damage")
    void activatesAndPreventsCombatDamage() {
        Permanent angus = addCreatureReady(player1, new AngusMackenzie());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(angus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.preventAllCombatDamage).isTrue();

        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new BarbaryApes());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can activate after blockers are declared and prevents damage to creatures and players")
    void activatesAfterBlockersAreDeclaredAndPreventsAllCombatDamage() {
        Permanent angus = addCreatureReady(player1, new AngusMackenzie());
        Permanent blocker = addCreatureReady(player1, new BarbaryApes());
        Permanent attacker = addCreatureReady(player2, new BarbaryApes());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker), 0)));

        addAngusActivationMana(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(angus), null, null);
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angus, blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Prevents combat damage only, not noncombat damage")
    void preventsCombatDamageOnly() {
        addCreatureReady(player1, new AngusMackenzie());
        addCreatureReady(player1, new PsionicEntity());
        harness.setLife(player2, 20);

        addAngusActivationMana(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Psionic Entity");
    }

    @Test
    @DisplayName("Cannot be activated at or after the combat damage step")
    void cannotActivateDuringCombatDamage() {
        addCreatureReady(player1, new AngusMackenzie());
        addAngusActivationMana(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before the combat damage step");
    }

    private void addAngusActivationMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
