package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiJackal.class, RagingGoblin.class})
class DauthiJackalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blocking creature")
    void destroysBlockingCreature() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Dauthi Jackal is sacrificed as part of the activation cost")
    void sacrificedAsCost() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());

        harness.assertNotOnBattlefield(player1, "Dauthi Jackal");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        addCreatureReady(player2, new RagingGoblin());

        var targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");

        harness.assertOnBattlefield(player1, "Dauthi Jackal");
        assertThat(gd.stack).isEmpty();
    }

    private void addBlackMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
    }

    private Permanent addBlocker() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        return blocker;
    }
}
