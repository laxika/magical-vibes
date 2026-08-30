package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingOracle.class, Forest.class, GrizzlyBears.class})
class CoilingOracleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a revealed land onto the battlefield")
    void landEntersBattlefield() {
        Card oracle = new CoilingOracle();
        Card land = new Forest();
        harness.setHand(player1, List.of(oracle));
        harness.setLibrary(player1, List.of(land, new GrizzlyBears()));
        addManaForOracle();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle)
                .anyMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("ETB puts a revealed nonland card into its controller's hand")
    void nonlandEntersHand() {
        Card oracle = new CoilingOracle();
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of(oracle));
        harness.setLibrary(player1, List.of(nonland, new Forest()));
        addManaForOracle();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle);
    }

    @Test
    @DisplayName("ETB does nothing when its controller's library is empty")
    void emptyLibraryDoesNothing() {
        Card oracle = new CoilingOracle();
        harness.setHand(player1, List.of(oracle));
        harness.setLibrary(player1, List.of());
        addManaForOracle();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == oracle);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addManaForOracle() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
