package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarragothBloodskySire.class, GrizzlyBears.class, Shock.class})
class VarragothBloodskySireTest extends BaseCardTest {

    @Test
    @DisplayName("Boast lets the target player search and put a card on top of their library")
    void boastSearchesTargetPlayersLibrary() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock()));
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock");

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Boast requires Varragoth to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new VarragothBloodskySire());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
