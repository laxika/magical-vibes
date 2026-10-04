package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HealerOfTheGlade.class, Unsummon.class})
class HealerOfTheGladeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when entering the battlefield")
    void gainsThreeLifeOnEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HealerOfTheGlade()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve and affects only its controller")
    void lifeGainUsesTheStack() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        harness.setHand(player1, List.of(new HealerOfTheGlade()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Healer of the Glade");
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Enter trigger still gains life after the creature returns to hand")
    void gainsLifeAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HealerOfTheGlade()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var healer = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player2, 0, healer.getId());
        harness.assertNotOnBattlefield(player1, "Healer of the Glade");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
