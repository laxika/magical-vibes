package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrollOfKhazadDum.class, GrizzlyBears.class, Swamp.class})
class TrollOfKhazadDumTest extends BaseCardTest {

    @Test
    @DisplayName("Troll of Khazad-dum can't be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent troll = addCreatureReady(player1, new TrollOfKhazadDum());
        addCreatureReady(player2, new GrizzlyBears());
        troll.setAttacking(true);

        forceBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @Test
    @DisplayName("Swampcycling discards Troll of Khazad-dum and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        TrollOfKhazadDum troll = new TrollOfKhazadDum();
        troll.setName("Troll of Khazad-dum");
        harness.setHand(player1, List.of(troll));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Troll of Khazad-dum");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Swamp");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    private void forceBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
