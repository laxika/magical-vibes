package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeFamiliar.class, WrathOfGod.class, Forest.class})
class FiligreeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("When Filigree Familiar enters, its controller gains 2 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new FiligreeFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("When Filigree Familiar dies, its controller draws a card")
    void drawsCardWhenItDies() {
        harness.addToBattlefield(player1, new FiligreeFamiliar());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Entering gains life only when the triggered ability resolves")
    void enteringUsesTheStackAndDoesNotDraw() {
        harness.setHand(player1, List.of(new FiligreeFamiliar()));
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castArtifact(player1, 0);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Filigree Familiar");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Simultaneous deaths draw one card for each Familiar's controller")
    void simultaneousDeathsDrawForEachController() {
        harness.addToBattlefield(player1, new FiligreeFamiliar());
        harness.addToBattlefield(player2, new FiligreeFamiliar());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, new Forest()));
        harness.setLibrary(player2, List.of(secondDraw, new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Filigree Familiar");
        harness.assertInGraveyard(player2, "Filigree Familiar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 15);
    }
}
