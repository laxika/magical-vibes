package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenBattlePriest.class, Disperse.class})
class AvenBattlePriestTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a triggered ability on the stack")
    void etbTriggers() {
        cast();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB gains 3 life")
    void etbGainsThreeLife() {
        cast();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Aven Battle Priest");
    }

    @Test
    @DisplayName("ETB still gains life after the Priest returns to hand")
    void etbGainsLifeAfterSourceLeavesBattlefield() {
        cast();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Aven Battle Priest").getId());

        harness.assertNotOnBattlefield(player1, "Aven Battle Priest");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    private void cast() {
        harness.setHand(player1, List.of(new AvenBattlePriest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
