package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WuWarship.class, Island.class, Forest.class, ShuFootSoldiers.class})
class WuWarshipTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Wu Warship onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new WuWarship(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wu Warship");
    }

    @Test
    @DisplayName("Wu Warship can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());
        addCreatureReady(player1, new WuWarship());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new WuWarship());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when only its controller controls an Island")
    void cannotAttackWhenOnlyControllerControlsIsland() {
        addCreatureReady(player1, new WuWarship());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when defending player controls a non-Island land")
    void cannotAttackWhenDefenderControlsNonIslandLand() {
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player1, new WuWarship());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wu Warship can attack when the defending player's Island is tapped")
    void canAttackWhenDefendersIslandIsTapped() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new Island()).setTapped(true);
        addCreatureReady(player1, new WuWarship());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Wu Warship can block when neither player controls an Island")
    void canBlockWithoutIslands() {
        addCreatureReady(player1, new ShuFootSoldiers());
        var warship = harness.addToBattlefieldAndReturn(player2, new WuWarship());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(warship.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wu Warship");
        harness.assertInGraveyard(player1, "Shu Foot Soldiers");
    }
}
