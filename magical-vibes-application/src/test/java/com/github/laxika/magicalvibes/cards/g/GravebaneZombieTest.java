package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravebaneZombieTest extends BaseCardTest {

    @Test
    @DisplayName("When Gravebane Zombie would die, it is put on top of its owner's library instead")
    void putOnTopOfLibraryInsteadOfDying() {
        Card filler = new CruelEdict();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player1, new GravebaneZombie());

        // Force sacrifice (a "die" event) via Cruel Edict
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Not on battlefield, and NOT in the graveyard — replacement effect applied
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");

        // Placed on TOP of its owner's library (index 0), above the pre-existing card
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(0).getName()).isEqualTo("Gravebane Zombie");
        assertThat(library.get(1)).isSameAs(filler);

        // Log confirms the replacement
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Gravebane Zombie") && log.contains("on top of its owner's library"));
    }
}
