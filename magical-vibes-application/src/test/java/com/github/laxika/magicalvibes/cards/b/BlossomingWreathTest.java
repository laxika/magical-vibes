package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abeyance;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlossomingWreath.class, Abeyance.class, BenalishInfantry.class})
class BlossomingWreathTest extends BaseCardTest {

    private void castWreath() {
        harness.castFromHand(player1, new BlossomingWreath(), "{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains life equal to the number of creature cards in the controller's graveyard")
    void gainsLifePerCreatureCard() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new BenalishInfantry(), new Abeyance()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Gains no life with no creature cards in the graveyard")
    void gainsNoLifeWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new Abeyance()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Ignores creature cards in an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));
        harness.setGraveyard(player2, List.of(new BenalishInfantry(), new BenalishInfantry()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }
}
