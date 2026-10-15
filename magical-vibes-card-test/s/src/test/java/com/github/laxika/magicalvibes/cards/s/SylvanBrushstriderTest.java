package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanBrushstrider.class, GrotesqueDemise.class})
class SylvanBrushstriderTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldGainsTwoLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SylvanBrushstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    void lifeGainWaitsForTheEnterTriggerToResolve() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new SylvanBrushstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sylvan Brushstrider");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastGainsLifeForItsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.enterBattlefieldAndReturn(player2, new SylvanBrushstrider());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }

    @Test
    void enterTriggerStillGainsLifeAfterTheCreatureIsExiled() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        var brushstrider = harness.enterBattlefieldAndReturn(player1, new SylvanBrushstrider());
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, brushstrider.getId());

        harness.assertNotOnBattlefield(player1, "Sylvan Brushstrider");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
    }
}
