package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DauthiJackalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blocking creature")
    void destroysBlockingCreature() {
        addJackal(player1);
        addBlackMana(player1);
        Permanent blocker = addBlocker(player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dauthi Jackal is sacrificed as part of the activation cost")
    void sacrificedAsCost() {
        addJackal(player1);
        addBlackMana(player1);
        Permanent blocker = addBlocker(player2);

        harness.activateAbility(player1, 0, null, blocker.getId());

        harness.assertNotOnBattlefield(player1, "Dauthi Jackal");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addJackal(player1);
        addBlackMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    private Permanent addJackal(Player player) {
        DauthiJackal card = new DauthiJackal();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addBlackMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
    }

    private Permanent addBlocker(Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        Permanent blocker = findPermanent(player, "Grizzly Bears");
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
