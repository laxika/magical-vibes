package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalGenesisTest extends BaseCardTest {

    @Test
    @DisplayName("The active player mills a card and creates Minions equal to its mana value")
    void activePlayerMillsAndCreatesTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new HillGiant())));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Hill Giant");
        assertThat(countPermanents(player2, "Minion")).isEqualTo(4);
        assertThat(countPermanents(player1, "Minion")).isZero();
    }

    @Test
    @DisplayName("A mana value zero card creates no Minions but is still milled")
    void manaValueZeroCreatesNoTokens() {
        harness.addToBattlefield(player1, new InfernalGenesis());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest())));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(countPermanents(player2, "Minion")).isZero();
    }
}
