package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({ThoughtpickerWitch.class, GrizzlyBears.class, Island.class, Forest.class})
class ThoughtpickerWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature exiles one of the top two cards of an opponent's library")
    void sacrificesCreatureAndExilesChosenCard() {
        harness.addToBattlefield(player1, new ThoughtpickerWitch());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card top = new Island();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(fodder.getCard().getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(second.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(top.getId(), third.getId());
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new ThoughtpickerWitch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("The ability can exile the only card in a short opponent library")
    void exilesOnlyAvailableCard() {
        harness.addToBattlefield(player1, new ThoughtpickerWitch());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card only = new Island();
        harness.setLibrary(player2, List.of(only));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(only.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
