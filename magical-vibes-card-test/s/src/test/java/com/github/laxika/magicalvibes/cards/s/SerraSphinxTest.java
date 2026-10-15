package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraSphinx.class, PoulticeSliver.class})
class SerraSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Remains untapped after attacking because of vigilance")
    void remainsUntappedAfterAttacking() {
        Permanent sphinx = addCreatureReady(player1, new SerraSphinx());

        declareAttackers(List.of(0));

        assertThat(sphinx.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking")
    void cannotBeBlockedByNonflyingCreature() {
        Permanent sphinx = addCreatureReady(player1, new SerraSphinx());
        Permanent blocker = addCreatureReady(player2, new PoulticeSliver());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sphinx);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature can block Serra Sphinx and both deal lethal damage")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new SerraSphinx());
        addCreatureReady(player2, new SerraSphinx());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Serra Sphinx");
        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertInGraveyard(player1, "Serra Sphinx");
        harness.assertInGraveyard(player2, "Serra Sphinx");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Serra Sphinx can block a nonflying attacker")
    void canBlockNonflyingCreature() {
        addCreatureReady(player1, new PoulticeSliver());
        addCreatureReady(player2, new SerraSphinx());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Poultice Sliver");
        harness.assertOnBattlefield(player2, "Serra Sphinx");
        harness.assertLife(player2, 20);
    }
}
