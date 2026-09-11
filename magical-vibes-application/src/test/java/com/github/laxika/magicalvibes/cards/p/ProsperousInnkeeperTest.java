package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperousInnkeeper.class, GrizzlyBears.class})
class ProsperousInnkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("When Prosperous Innkeeper enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new ProsperousInnkeeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeWhenAnotherCreatureEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when Prosperous Innkeeper itself enters")
    void doesNotGainLifeOnOwnEntry() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ProsperousInnkeeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void doesNotGainLifeOnOpponentCreatureEntry() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ProsperousInnkeeper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
