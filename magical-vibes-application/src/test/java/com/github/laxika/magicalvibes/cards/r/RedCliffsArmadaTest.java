package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedCliffsArmada.class, Island.class})
class RedCliffsArmadaTest extends BaseCardTest {

    @Test
    @DisplayName("Red Cliffs Armada can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new RedCliffsArmada());
        declareAttackers(List.of(0));

        // 5/4 unblocked attacker deals 5 to defending player.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Red Cliffs Armada cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new RedCliffsArmada());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red Cliffs Armada cannot attack when only attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new RedCliffsArmada());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Island still permits Red Cliffs Armada to attack")
    void canAttackWhenDefendersIslandIsTapped() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new Island()).setTapped(true);
        addCreatureReady(player1, new RedCliffsArmada());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Islands in the defending player's hand and graveyard do not permit attacking")
    void cannotAttackWithIslandsOutsideBattlefield() {
        harness.setHand(player2, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new RedCliffsArmada());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Island restriction uses the defending player when player two attacks")
    void canAttackAsPlayerTwoWhenPlayerOneControlsIsland() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2, new RedCliffsArmada());

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 15);
    }
}
