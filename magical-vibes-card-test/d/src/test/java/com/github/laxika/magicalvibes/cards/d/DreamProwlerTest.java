package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamProwler.class, SkyshroudFalcon.class})
class DreamProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Dream Prowler can't be blocked while attacking alone")
    void cantBeBlockedWhenAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        addCreatureReady(player1, new SkyshroudFalcon());
        Permanent prowler = addCreatureReady(player1, new DreamProwler());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(prowler)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Dream Prowler can be blocked when attacking alongside another creature")
    void canBeBlockedWhenNotAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());

        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        addCreatureReady(player1, new SkyshroudFalcon());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Dream Prowler deals combat damage when attacking alone")
    void dealsDamageWhenUnblockedAlone() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new DreamProwler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @CardUsed({DreamProwler.class, Boomerang.class})
    @DisplayName("Dream Prowler becomes unblockable when the other attacker leaves before blocks")
    void becomesUnblockableWhenOtherAttackerLeaves() {
        Permanent blocker = addCreatureReady(player2, new DreamProwler());
        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        Permanent otherAttacker = addCreatureReady(player1, new DreamProwler());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.castAndResolveInstant(player1, 0, otherAttacker.getId());
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @CardUsed({DreamProwler.class, Boomerang.class})
    @DisplayName("Becoming the only attacker after blocks does not undo an existing block")
    void remainsBlockedWhenOtherAttackerLeavesAfterBlocks() {
        Permanent blocker = addCreatureReady(player2, new DreamProwler());
        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        Permanent otherAttacker = addCreatureReady(player1, new DreamProwler());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                    gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                    gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));
            harness.castAndResolveInstant(player1, 0, otherAttacker.getId());
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();

        harness.assertLife(player2, 20);
    }
}
