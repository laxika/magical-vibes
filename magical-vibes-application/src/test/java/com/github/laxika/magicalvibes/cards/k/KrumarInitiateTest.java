package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KrumarInitiate.class)
class KrumarInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Krumar Initiate pays X life and endures X with counters")
    void enduresWithCounters() {
        Permanent initiate = addCreatureReady(player1, new KrumarInitiate());
        harness.setLife(player1, 20);
        addManaForX(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Krumar Initiate pays X life and endures X with a Spirit")
    void enduresWithSpirit() {
        addCreatureReady(player1, new KrumarInitiate());
        harness.setLife(player1, 20);
        addManaForX(3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 3/3 white Spirit creature token");

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Krumar Initiate cannot pay more life than its controller has")
    void cannotPayMoreLifeThanAvailable() {
        Permanent initiate = addCreatureReady(player1, new KrumarInitiate());
        harness.setLife(player1, 1);
        addManaForX(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(initiate.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, x);
    }
}
