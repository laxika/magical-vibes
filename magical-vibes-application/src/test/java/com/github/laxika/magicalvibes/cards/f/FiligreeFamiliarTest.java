package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiligreeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("When Filigree Familiar enters, its controller gains 2 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new FiligreeFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("When Filigree Familiar dies, its controller draws a card")
    void drawsCardWhenItDies() {
        harness.addToBattlefield(player1, new FiligreeFamiliar());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }
}
