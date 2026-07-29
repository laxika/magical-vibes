package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestlessDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingAbilityGrantsShield() {
        addRestlessDeadReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addRestlessDeadReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Restless Dead from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent dead = addRestlessDeadReady(player1);
        dead.setRegenerationShield(1);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Restless Dead");
        Permanent survivor = findPermanent(player1, "Restless Dead");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Without a regeneration shield Restless Dead dies in combat")
    void diesWithoutShield() {
        Permanent dead = addRestlessDeadReady(player1);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Restless Dead");
        harness.assertInGraveyard(player1, "Restless Dead");
    }

    private Permanent addRestlessDeadReady(Player player) {
        Permanent perm = new Permanent(new RestlessDead());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
