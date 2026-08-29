package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScornEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn for no mana")
    void foretellsAndCastsOnLaterTurn() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(effigy.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.castFromExile(player1, effigy.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scorn Effigy");
    }
}
