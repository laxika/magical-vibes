package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LifebaneZombieTest extends BaseCardTest {

    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LifebaneZombie()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> hand reveal + choice prompt
    }

    @Test
    @DisplayName("Choosing a green creature card exiles it permanently")
    void choosingGreenCreatureExilesIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).exileMode()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Peek");
    }

    @Test
    @DisplayName("Only green and white creature cards are valid choices")
    void onlyGreenAndWhiteCreaturesAreValid() {
        Card green = new GrizzlyBears();
        Card black = new ChildOfNight();
        Card white = new SerraAngel();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(green, black, white, instant)));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("Hand without green or white creatures gives no choices")
    void noValidChoicesWhenNoGreenOrWhiteCreatures() {
        harness.setHand(player2, new ArrayList<>(List.of(new ChildOfNight(), new Peek())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Exiled card does not return when the Zombie dies")
    void exiledCardStaysExiledWhenZombieDies() {
        harness.setHand(player2, new ArrayList<>(List.of(new SerraAngel())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID zombieId = harness.getPermanentId(player1, "Lifebane Zombie");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, zombieId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lifebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
