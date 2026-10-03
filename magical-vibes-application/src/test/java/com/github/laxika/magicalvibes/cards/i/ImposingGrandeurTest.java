package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImposingGrandeur.class, EdgarMarkov.class, GrizzlyBears.class})
class ImposingGrandeurTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws according to the greatest mana value of their own commander")
    void drawsForEachPlayersCommander() {
        Card player1Commander = new GrizzlyBears();
        Card player2Commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), player1Commander);
        gd.makeCommander(player2.getId(), player2Commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(player1Commander)));
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>(List.of(player2Commander)));

        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ImposingGrandeur(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 2);
        fillLibrary(player2, 6);
        addGrandeurMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1HandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2HandCard);
    }

    @Test
    @DisplayName("Declining leaves a player's hand unchanged")
    void mayBeDeclined() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));

        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ImposingGrandeur(), handCard));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        fillLibrary(player1, 2);
        fillLibrary(player2, 2);
        addGrandeurMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(handCard);
    }

    private void addGrandeurMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void fillLibrary(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setLibrary(player, cards);
    }
}
