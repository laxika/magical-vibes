package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlossomingWreathTest extends BaseCardTest {

    private void castWreath() {
        harness.setHand(player1, List.of(new BlossomingWreath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains life equal to the number of creature cards in the controller's graveyard")
    void gainsLifePerCreatureCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Gains no life with no creature cards in the graveyard")
    void gainsNoLifeWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new Forest()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Ignores creature cards in an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        castWreath();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }
}
