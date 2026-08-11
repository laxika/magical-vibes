package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnhandEulogistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards and drains when the graveyard contains an Elf")
    void etbMillsAndDrainsWithMilledElf() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DawnhandEulogist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB drains when an Elf was already in the graveyard")
    void etbDrainsWithExistingElf() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new DawnhandEulogist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not drain when the graveyard contains no Elf")
    void etbDoesNotDrainWithoutElf() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DawnhandEulogist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
