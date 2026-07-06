package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArnynDeathbloomBotanistTest extends BaseCardTest {

    

    @Test
    @DisplayName("When a 1/1 creature you control dies, target opponent loses 2 life and you gain 2 life")
    void smallAllyDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castInstant(player2, 0, elvesId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not trigger when a creature without power or toughness 1 or less dies")
    void largeAllyDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Arnyn's own death at 2/2 does not trigger")
    void ownDeathAtTwoTwoDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID arnynId = harness.getPermanentId(player1, "Arnyn, Deathbloom Botanist");
        harness.castInstant(player2, 0, arnynId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
