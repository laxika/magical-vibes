package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhirapurAetherGridTest extends BaseCardTest {

    private static final int PING_ABILITY = 0;

    @Test
    @DisplayName("Deals 1 damage to target player, tapping two artifacts as the cost")
    void pingsPlayer() {
        Permanent grid = addGrid(player1);
        Permanent artifact1 = addArtifact(player1);
        Permanent artifact2 = addArtifact(player1);
        prepareMainPhase();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(grid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature, killing a 1/1")
    void pingsCreature() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        addArtifact(player1);
        prepareMainPhase();

        harness.addToBattlefield(player2, new SavannahLions());
        UUID victim = harness.getPermanentId(player2, "Savannah Lions");

        harness.activateAbility(player1, indexOf(player1, grid), PING_ABILITY, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Cannot activate without two untapped artifacts")
    void requiresTwoUntappedArtifacts() {
        Permanent grid = addGrid(player1);
        addArtifact(player1);
        Permanent tapped = addArtifact(player1);
        tapped.tap();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, grid), PING_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addGrid(Player player) {
        Permanent perm = new Permanent(new GhirapurAetherGrid());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        Permanent perm = new Permanent(new Ornithopter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
