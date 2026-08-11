package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScarbladeScoutTest extends BaseCardTest {

    @Test
    void etbMillsTwoCardsFromControllerLibrary() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();

        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(f1, f2));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 2);
    }

    @Test
    void etbMillsOnlyAvailableCards() {
        Forest forest = new Forest();

        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }
}
