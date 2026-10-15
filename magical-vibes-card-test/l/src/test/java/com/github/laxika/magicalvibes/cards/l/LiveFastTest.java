package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiveFast.class, Forest.class})
class LiveFastTest extends BaseCardTest {

    @Test
    void drawsTwoLosesTwoLifeAndGainsTwoEnergy() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Live Fast");
    }

    @Test
    void addsEnergyToExistingCountersAndOnlyAffectsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        gd.setPlayerEnergyCounters(player1.getId(), 3);
        gd.setPlayerEnergyCounters(player2.getId(), 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LiveFast()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 8);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 17);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        harness.assertInGraveyard(player1, "Live Fast");
    }
}
