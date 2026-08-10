package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VulshokGauntletsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature does not untap during its controller's untap step")
    void equippedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent free = addCreatureReady(player1, new GrizzlyBears());
        free.tap();

        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature untaps after Vulshok Gauntlets is removed")
    void equippedCreatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent gauntlets = addGauntletsReady(player1);
        gauntlets.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gauntlets);

        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addGauntletsReady(Player player) {
        Permanent perm = new Permanent(new VulshokGauntlets());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
