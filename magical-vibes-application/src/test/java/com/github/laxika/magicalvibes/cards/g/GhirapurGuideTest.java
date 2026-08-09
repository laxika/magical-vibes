package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhirapurGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot be blocked by a creature with power 2 or less")
    void lowPowerCreatureCannotBlock() {
        Permanent guide = addCreatureReady(player1, new GhirapurGuide());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        activate(guide, target);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target creature can be blocked by a creature with power 3 or greater")
    void highPowerCreatureCanBlock() {
        Permanent guide = addCreatureReady(player1, new GhirapurGuide());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        activate(guide, target);
        target.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, target);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent guide = addCreatureReady(player1, new GhirapurGuide());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                indexOf(player1, guide), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent guide, Permanent target) {
        addActivationMana();
        harness.activateAbility(player1, indexOf(player1, guide), null, target.getId());
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
