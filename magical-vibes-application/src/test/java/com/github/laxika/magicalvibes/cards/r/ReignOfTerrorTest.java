package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GibberingHyenas;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ReignOfTerror.class, GibberingHyenas.class, IronTuskElephant.class})
class ReignOfTerrorTest extends BaseCardTest {

    private void castReign(int mode) {
        harness.setHand(player1, List.of(new ReignOfTerror()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Green mode destroys only green creatures and costs 2 life each")
    void greenMode() {
        harness.addToBattlefield(player1, new GibberingHyenas());
        harness.addToBattlefield(player2, new GibberingHyenas());
        harness.addToBattlefield(player2, new IronTuskElephant());

        castReign(0);

        harness.assertNotOnBattlefield(player1, "Gibbering Hyenas");
        harness.assertNotOnBattlefield(player2, "Gibbering Hyenas");
        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White mode destroys only white creatures and costs 2 life each")
    void whiteMode() {
        harness.addToBattlefield(player1, new IronTuskElephant());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.addToBattlefield(player2, new GibberingHyenas());

        castReign(1);

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertNotOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertOnBattlefield(player2, "Gibbering Hyenas");

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No matching creatures means no life loss")
    void noCreaturesNoLifeLoss() {
        harness.addToBattlefield(player2, new IronTuskElephant());

        castReign(0);

        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Green creatures can't be regenerated after the green mode destroys them")
    void cannotBeRegenerated() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new GibberingHyenas());
        greenCreature.setRegenerationShield(1);

        castReign(0);

        harness.assertNotOnBattlefield(player2, "Gibbering Hyenas");
        harness.assertInGraveyard(player2, "Gibbering Hyenas");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
