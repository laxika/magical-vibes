package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScaldTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping your Island for mana deals 1 damage to you")
    void controllerTapsIsland() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Island());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping an opponent's Island for mana deals 1 damage to that opponent")
    void opponentTapsIsland() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping a non-Island land for mana does not trigger Scald")
    void nonIslandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Island tap triggers Scald separately")
    void multipleIslandTapsTriggerSeparately() {
        harness.addToBattlefield(player1, new Scald());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.tapPermanent(player1, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
