package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, Island.class, SeaMonster.class, AvianChangeling.class})
class SeaMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sea Monster puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new SeaMonster(), "{4}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SeaMonster.class);
    }

    @Test
    @DisplayName("Resolving puts Sea Monster onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new SeaMonster(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sea Monster");
    }

    @Test
    @DisplayName("Sea Monster enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new SeaMonster(), "{4}{U}{U}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Sea Monster");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Sea Monster can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SeaMonster());
        declareAttackers(List.of(0));

        // Combat auto-advances; verify attack went through by checking damage dealt
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Sea Monster can attack when defending player controls a tapped Island")
    void canAttackWhenDefenderControlsTappedIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new Island()).tap();

        addCreatureReady(player1, new SeaMonster());
        declareAttackers(List.of(0));

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Sea Monster cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackerControlsIsland() {
        addCreatureReady(player1, new SeaMonster());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster cannot attack if defender controls only a non-Island creature")
    void cannotAttackWhenDefenderOnlyControlsNonIslandCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblocked Sea Monster deals 6 damage to defending player")
    void dealsSixDamageWhenUnblocked() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SeaMonster());
        declareAttackers(List.of(0));

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Sea Monster cannot attack if defender controls only a changeling creature")
    @CardUsed({AvianChangeling.class, SeaMonster.class})
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        harness.addToBattlefield(player2, new AvianChangeling());
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Island in the defending player's graveyard does not allow an attack")
    void cannotAttackWhenIslandIsOnlyInDefendersGraveyard() {
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster can block without either player controlling an Island")
    void canBlockWithoutIslands() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SeaMonster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sea Monster");
    }

    @Test
    @DisplayName("Sea Monster checks the defending player when player two attacks")
    void canAttackFromOtherPlayersBattlefield() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2, new SeaMonster());

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 14);
    }
}
