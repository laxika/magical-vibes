package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircuDimirLobotomist.class, DarkRitual.class, Divination.class, GrizzlyBears.class})
class CircuDimirLobotomistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a blue spell exiles the top card of the chosen player's library with Circu")
    void blueSpellExilesTopCardWithCircu() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a black spell exiles the top card of the chosen player's library with Circu")
    void blackSpellExilesTopCardWithCircu() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Opponents cannot cast a spell with the name of a card exiled with Circu")
    void opponentCannotCastSpellWithExiledName() {
        Permanent circu = harness.addToBattlefieldAndReturn(player1, new CircuDimirLobotomist());
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(circu.getId())).containsExactly(exiledCard);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
