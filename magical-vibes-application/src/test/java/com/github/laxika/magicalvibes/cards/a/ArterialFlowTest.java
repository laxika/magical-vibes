package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OrazcaRaptor;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArterialFlow.class, DuskLegionZealot.class, OrazcaRaptor.class, MomentOfCraving.class})
class ArterialFlowTest extends BaseCardTest {

    private void castArterialFlow() {
        harness.setHand(player1, List.of(new ArterialFlow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Each opponent discards two cards")
    void eachOpponentDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new OrazcaRaptor(), new DuskLegionZealot(), new OrazcaRaptor())));

        castArterialFlow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With a Vampire, each opponent loses 2 life and controller gains 2 life")
    void vampireAddsLifeRider() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castArterialFlow();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Without a Vampire, the life rider does not happen")
    void noVampireNoLifeRider() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castArterialFlow();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Life changes wait until both discard choices finish")
    void lifeRiderWaitsForDiscardToFinish() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player2, List.of(new OrazcaRaptor(), new DuskLegionZealot(), new OrazcaRaptor()));
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        castArterialFlow();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 0);
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Arterial Flow");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent with only one card discards it and still loses 2 life")
    void shortHandDoesNotReduceLifeRider() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player2, List.of(new OrazcaRaptor()));
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        castArterialFlow();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Orazca Raptor");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Vampire controlled only by the opponent does not enable the life rider")
    void opponentsVampireDoesNotEnableLifeRider() {
        harness.addToBattlefield(player2, new DuskLegionZealot());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        castArterialFlow();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Vampires still cause only 2 life to be lost and gained")
    void multipleVampiresDoNotMultiplyLifeRider() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        castArterialFlow();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The controller keeps their hand and a Vampire in hand does not enable the life rider")
    void controllerDoesNotDiscardAndVampireInHandDoesNotCount() {
        harness.setHand(player1, List.of(new ArterialFlow(), new DuskLegionZealot(), new OrazcaRaptor()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Dusk Legion Zealot");
        harness.assertInHand(player1, "Orazca Raptor");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing the last Vampire in response disables the life rider")
    void vampireConditionIsCheckedAtResolution() {
        var vampire = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new ArterialFlow()));
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.castInstant(player2, 0, vampire.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Legion Zealot");
        harness.assertLife(player2, 22);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arterial Flow");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 22);
    }
}
