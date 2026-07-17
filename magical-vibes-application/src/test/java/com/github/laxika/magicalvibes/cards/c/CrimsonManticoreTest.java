package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrimsonManticoreTest extends BaseCardTest {

    private void addReadyManticore() {
        harness.addToBattlefield(player1, new CrimsonManticore());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
    }

    private Permanent addAttacker(Player owner) {
        harness.addToBattlefield(owner, new FugitiveWizard());
        Permanent attacker = gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        harness.addToBattlefield(owner, new FugitiveWizard());
        Permanent blocker = gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();
        blocker.setSummoningSick(false);
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
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Deals 1 damage to a target blocking creature")
    void damagesBlocker() {
        addReadyManticore();
        Permanent blocker = addBlocker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadyManticore();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap — cannot activate if already tapped")
    void cannotActivateIfTapped() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
