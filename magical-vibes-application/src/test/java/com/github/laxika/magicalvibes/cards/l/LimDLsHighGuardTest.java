package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.e.EnslavedScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LimDLsHighGuard.class, GorillaChieftain.class, EnslavedScout.class})
class LimDLsHighGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingAbilityGrantsShield() {
        addCreatureReady(player1, new LimDLsHighGuard());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability costs {1}{B}")
    void abilityCostsOneAndBlack() {
        addCreatureReady(player1, new LimDLsHighGuard());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability requires black mana")
    void abilityRequiresBlackMana() {
        addCreatureReady(player1, new LimDLsHighGuard());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal combat damage")
    void shieldSavesFromCombatDamage() {
        Permanent guard = addCreatureReady(player1, new LimDLsHighGuard());
        guard.setRegenerationShield(1);
        guard.setBlocking(true);
        guard.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GorillaChieftain());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Lim-Dûl's High Guard");
        Permanent survivor = findPermanent(player1, "Lim-Dûl's High Guard");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals damage back")
    void firstStrikeKillsBlockerFirst() {
        Permanent guard = addCreatureReady(player1, new LimDLsHighGuard());
        guard.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new EnslavedScout());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        // 2 first-strike damage is lethal to a 2/2, so the blocker dies before dealing damage back
        harness.assertOnBattlefield(player1, "Lim-Dûl's High Guard");
        harness.assertNotOnBattlefield(player2, "Enslaved Scout");
    }

    @Test
    @DisplayName("Without a shield it dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent guard = addCreatureReady(player1, new LimDLsHighGuard());
        guard.setBlocking(true);
        guard.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GorillaChieftain());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Lim-Dûl's High Guard");
        harness.assertInGraveyard(player1, "Lim-Dûl's High Guard");
    }

}
