package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonManticore.class, TundraWolves.class})
class CrimsonManticoreTest extends BaseCardTest {

    private Permanent addReadyManticore() {
        return addCreatureReady(player1, new CrimsonManticore());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new TundraWolves());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new TundraWolves());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

    @Test
    @DisplayName("Deals 1 damage to a target attacking creature")
    void damagesAttacker() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // 1 damage kills the 1/1 attacker
        harness.assertNotOnBattlefield(player2, "Tundra Wolves");
        harness.assertInGraveyard(player2, "Tundra Wolves");
    }

    @Test
    @DisplayName("Deals 1 damage to a target blocking creature")
    void damagesBlocker() {
        addReadyManticore();
        Permanent blocker = addBlocker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tundra Wolves");
        harness.assertInGraveyard(player2, "Tundra Wolves");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadyManticore();
        harness.addToBattlefield(player2, new TundraWolves());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID wolfId = harness.getPermanentId(player2, "Tundra Wolves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wolfId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap — cannot activate if already tapped")
    void cannotActivateIfTapped() {
        Permanent manticore = addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        manticore.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires red mana and cannot activate with only colorless mana")
    void cannotActivateWithoutRedMana() {
        Permanent manticore = addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(manticore.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target must still be attacking or blocking when the ability resolves")
    void targetMustStillBeInCombatOnResolution() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tundra Wolves");
        harness.assertNotInGraveyard(player2, "Tundra Wolves");
    }
}
