package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildrenOfKorlis.class, Shock.class})
class ChildrenOfKorlisTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the life its controller lost this turn")
    void gainsLifeEqualToControllersLifeLostThisTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChildrenOfKorlis());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Children of Korlis");
        harness.assertInGraveyard(player1, "Children of Korlis");
    }

    @Test
    @DisplayName("Gains no life when its controller lost no life this turn")
    void gainsNoLifeWhenControllerLostNoLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ChildrenOfKorlis());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Children of Korlis");
        harness.assertInGraveyard(player1, "Children of Korlis");
    }
}
