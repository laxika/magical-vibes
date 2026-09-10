package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudSpirit.class, SkyshroudFalcon.class, SpinedWurm.class})
class CloudSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Cloud Spirit onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new CloudSpirit(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cloud Spirit");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new CloudSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cloud Spirit can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent spiritPerm = addCreatureReady(player2, new CloudSpirit());

        addCreatureReady(player1, new SkyshroudFalcon());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spiritPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Spirit cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new CloudSpirit());

        addCreatureReady(player1, new SpinedWurm());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Cloud Spirit")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new CloudSpirit());
        addCreatureReady(player2, new SpinedWurm());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Unblocked Cloud Spirit deals 3 damage to defending player")
    void dealsThreeDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new CloudSpirit());
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
