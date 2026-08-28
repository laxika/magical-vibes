package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShatterTheSky.class, ColossalDreadmaw.class, GrizzlyBears.class})
class ShatterTheSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with a creature of power 4 or greater draws before all creatures are destroyed")
    void qualifyingPlayersDrawBeforeBoardWipe() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.setHand(player2, List.of());
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize);
        harness.assertNotOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw for players whose creatures all have power less than 4")
    void nonqualifyingPlayersDoNotDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.setHand(player2, List.of());
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
