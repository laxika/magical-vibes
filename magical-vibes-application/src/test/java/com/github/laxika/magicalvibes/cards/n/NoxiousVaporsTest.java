package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gainsay;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MorgueToad;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SamitePilgrim;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoxiousVaporsTest extends BaseCardTest {

    @Test
    void keepsOneCardForEachColorAndAllowsMulticoloredCardToFillSeveralChoices() {
        Card multicolored = new Terminate();
        Card blue = new Gainsay();
        Card red = new MorgueToad();
        Card extraWhite = new SamitePilgrim();
        Card green = new GiantGrowth();
        Card colorless = new Ornithopter();
        Card land = new Island();

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousVapors(), multicolored, blue, red,
                extraWhite, green, colorless, land));
        harness.setHand(player2, List.of(new Gainsay(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class);

        // WUBRG: the same Terminate is selected again for red.
        harness.handleCardChosen(player1, 3); // white: Samite Pilgrim
        harness.handleCardChosen(player1, 1); // blue: Gainsay
        harness.handleCardChosen(player1, 0); // black: Terminate
        harness.handleCardChosen(player1, 0); // red: Terminate
        harness.handleCardChosen(player1, 4); // green: Giant Growth

        // Player 2 has only a blue nonland card to choose.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Terminate", "Gainsay", "Samite Pilgrim", "Giant Growth", "Island");
        harness.assertInGraveyard(player1, "Morgue Toad");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Gainsay", "Forest");
    }

    @Test
    void discardsColorlessNonlandsAndKeepsLandsWithoutAChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousVapors(), new Ornithopter(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        harness.assertInGraveyard(player1, "Ornithopter");
    }
}
