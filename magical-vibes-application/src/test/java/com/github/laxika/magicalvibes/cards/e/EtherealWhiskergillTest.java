package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealWhiskergill.class, Island.class})
class EtherealWhiskergillTest extends BaseCardTest {

    @Test
    @DisplayName("Ethereal Whiskergill can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new EtherealWhiskergill());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ethereal Whiskergill cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new EtherealWhiskergill());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ethereal Whiskergill cannot attack when only attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new EtherealWhiskergill());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
