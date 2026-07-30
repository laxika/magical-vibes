package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightErrant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls a Soldier")
    void canAttackWithSoldier() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new EliteVanguard());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can attack when controller controls a Knight")
    void canAttackWithKnight() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new KnightErrant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot attack without a Knight or Soldier")
    void cannotAttackWithoutKnightOrSoldier() {
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the opponent controls a Soldier")
    void cannotAttackWhenOnlyOpponentControlsSoldier() {
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player2, new EliteVanguard());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
