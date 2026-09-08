package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NissasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("When Nissa's Chosen would die, it is put on the bottom of its owner's library instead")
    void putOnBottomOfLibraryInsteadOfDying() {
        Card filler = new CruelEdict();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player1, new NissasChosen());

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.assertNotInGraveyard(player1, "Nissa's Chosen");

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(0)).isSameAs(filler);
        assertThat(library.get(1).getName()).isEqualTo("Nissa's Chosen");
    }
}
