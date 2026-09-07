package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShinraReinforcements.class, Forest.class, GrizzlyBears.class})
class ShinraReinforcementsTest extends BaseCardTest {

    @Test
    void entersAndMillsThreeCardsAndGainsThreeLife() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ShinraReinforcements()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
