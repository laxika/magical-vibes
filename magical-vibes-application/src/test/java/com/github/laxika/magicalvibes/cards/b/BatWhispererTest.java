package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BatWhisperer.class)
class BatWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a Bat when no opponent lost life this turn")
    void noBatWithoutOpponentLifeLoss() {
        castBatWhisperer();

        assertThat(countPermanents(player1, "Bat")).isZero();
    }

    @Test
    @DisplayName("Creates a 1/1 black Bat creature token with flying after an opponent lost life")
    void createsBatAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castBatWhisperer();

        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getEffectivePower()).isEqualTo(1);
        assertThat(bat.getEffectiveToughness()).isEqualTo(1);
        assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not create a Bat when only you lost life this turn")
    void noBatFromControllerLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        castBatWhisperer();

        assertThat(countPermanents(player1, "Bat")).isZero();
    }

    private void castBatWhisperer() {
        harness.setHand(player1, List.of(new BatWhisperer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
