package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuresightMerrow.class, GrizzlyBears.class, LlanowarElves.class})
class PuresightMerrowTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling the looked-at card removes it from the library and untaps the source ({Q})")
    void exilesTopCardOfOwnLibrary() {
        Permanent merrow = addTapped(player1, new PuresightMerrow());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new LlanowarElves()));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Choose to exile the top card.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(merrow.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the card on top of the library")
    void mayDeclineToExile() {
        addTapped(player1, new PuresightMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new LlanowarElves()));

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Decline: -1 means "fail to find" — nothing is exiled.
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getId()).isEqualTo(top.getId());
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new PuresightMerrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }


    @Test
    @DisplayName("The untap cost is paid before the ability resolves")
    void untapsWhenActivated() {
        Permanent merrow = addTapped(player1, new PuresightMerrow());
        Card top = new PuresightMerrow();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLUE, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(merrow.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness cannot pay the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent merrow = harness.addToBattlefieldAndReturn(player1, new PuresightMerrow());
        merrow.setSummoningSick(true);
        merrow.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(merrow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activation or create an exile choice")
    void resolvesWithEmptyLibrary() {
        Permanent merrow = addTapped(player1, new PuresightMerrow());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(merrow.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("On an opponent's turn the ability still looks at its controller's library")
    void usesControllerLibraryOnOpponentsTurn() {
        addTapped(player1, new PuresightMerrow());
        Card ownTop = new PuresightMerrow();
        Card opposingTop = new PuresightMerrow();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop));
        harness.addMana(player1, ManaColor.BLUE, 1);
        enterMainWithPriority(player2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
