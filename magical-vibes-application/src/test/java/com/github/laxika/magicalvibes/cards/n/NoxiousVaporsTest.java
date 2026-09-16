package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.g.GaeasMight;
import com.github.laxika.magicalvibes.cards.g.Gainsay;
import com.github.laxika.magicalvibes.cards.m.MorgueToad;
import com.github.laxika.magicalvibes.cards.s.SamitePilgrim;
import com.github.laxika.magicalvibes.cards.s.StarCompass;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousVapors.class, Gainsay.class, GaeasMight.class, MorgueToad.class,
        ForsakenCity.class, StarCompass.class, SamitePilgrim.class, Terminate.class})
class NoxiousVaporsTest extends BaseCardTest {

    @Test
    void keepsOneCardForEachColorAndAllowsMulticoloredCardToFillSeveralChoices() {
        Card multicolored = new Terminate();
        Card blue = new Gainsay();
        Card extraBlack = new MorgueToad();
        Card extraWhite = new SamitePilgrim();
        Card green = new GaeasMight();
        Card colorless = new StarCompass();
        Card land = new ForsakenCity();

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousVapors(), multicolored, blue, extraBlack,
                extraWhite, green, colorless, land));
        harness.setHand(player2, List.of(new Gainsay(), new ForsakenCity()));
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
        harness.handleCardChosen(player1, 4); // green: Gaea's Might

        // Player 2 has only a blue nonland card to choose.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains(player1.getUsername() + " reveals their hand")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " reveals their hand")).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Terminate", "Gainsay", "Samite Pilgrim", "Gaea's Might", "Forsaken City");
        harness.assertInGraveyard(player1, "Morgue Toad");
        harness.assertInGraveyard(player1, "Star Compass");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Gainsay", "Forsaken City");
    }

    @Test
    void discardsColorlessNonlandsAndKeepsLandsWithoutAChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousVapors(), new StarCompass(), new ForsakenCity()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forsaken City");
        harness.assertInGraveyard(player1, "Star Compass");
    }

    @Test
    void discardsUnchosenNonlandsFromTheNonactivePlayersHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousVapors()));
        harness.setHand(player2, List.of(new Gainsay(), new Gainsay(), new ForsakenCity()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Gainsay", "Forsaken City");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Gainsay");
    }
}
