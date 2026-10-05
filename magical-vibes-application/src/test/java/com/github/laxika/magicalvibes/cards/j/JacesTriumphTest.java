package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaticMentor;
import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesTriumph.class, JaceWielderOfMysteries.class, PouncingLynx.class, KasminaEnigmaticMentor.class})
class JacesTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards without a Jace planeswalker")
    void drawsTwoWithoutJace() {
        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws three cards while controlling a Jace planeswalker")
    void drawsThreeWithJace() {
        addJace(player1);

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not count an opponent's Jace planeswalker")
    void opponentJaceDoesNotCount() {
        addJace(player2);

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A different planeswalker does not increase the draw")
    void otherPlaneswalkerDoesNotCount() {
        Permanent kasmina = harness.addToBattlefieldAndReturn(player1, new KasminaEnigmaticMentor());
        kasmina.setCounterCount(CounterType.LOYALTY, 5);

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A Jace in the graveyard does not increase the draw")
    void graveyardJaceDoesNotCount() {
        harness.setGraveyard(player1, List.of(new JaceWielderOfMysteries()));

        castTriumph(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Checks for Jace at resolution after Jace leaves the battlefield")
    void drawsTwoWhenJaceLeavesBeforeResolution() {
        Permanent jace = addJace(player1);
        prepareTriumph(player1);
        harness.castSorcery(player1, 0, 0);
        gd.playerBattlefields.get(player1.getId()).remove(jace);
        harness.setGraveyard(player1, List.of(jace.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Checks for Jace at resolution after Jace enters the battlefield")
    void drawsThreeWhenJaceEntersBeforeResolution() {
        prepareTriumph(player1);
        harness.castSorcery(player1, 0, 0);
        addJace(player1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private void castTriumph(Player player) {
        prepareTriumph(player);
        harness.castAndResolveSorcery(player, 0, 0);
    }

    private void prepareTriumph(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new JacesTriumph()));
        harness.setLibrary(player, List.of(new PouncingLynx(), new PouncingLynx(), new PouncingLynx()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addJace(Player player) {
        Permanent jace = harness.addToBattlefieldAndReturn(player, new JaceWielderOfMysteries());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        return jace;
    }
}
