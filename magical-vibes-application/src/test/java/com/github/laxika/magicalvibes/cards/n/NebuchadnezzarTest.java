package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NebuchadnezzarTest extends BaseCardTest {

    @Test
    @DisplayName("Discards every matching card among the revealed cards")
    void discardsMatchingRevealedCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest())));
        readyNebuchadnezzar(3);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.ChooseNameRevealRandomCardsDiscardMatchingChoice.class);

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Reveals no more cards than the paid X")
    void revealsPaidXCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        readyNebuchadnezzar(2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can only be activated during its controller's turn")
    void onlyActivatesDuringYourTurn() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addReadyNebuchadnezzar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyNebuchadnezzar(int mana) {
        addReadyNebuchadnezzar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addReadyNebuchadnezzar(Player player) {
        var permanent = new com.github.laxika.magicalvibes.model.Permanent(new Nebuchadnezzar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
