package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArbalestEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to an attacking creature, killing a 2/2")
    void damagesAttackingCreature() {
        addReadyElite(player1);
        addMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        findPermanent(player2, "Grizzly Bears").setAttacking(true);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activating taps the Elite and keeps it from untapping next untap step")
    void activationExertsSelf() {
        Permanent elite = addReadyElite(player1);
        addMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        findPermanent(player2, "Grizzly Bears").setBlocking(true);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(elite.isTapped()).isTrue();
        assertThat(elite.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addReadyElite(player1);
        addMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyElite(Player player) {
        Permanent perm = new Permanent(new ArbalestElite());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
