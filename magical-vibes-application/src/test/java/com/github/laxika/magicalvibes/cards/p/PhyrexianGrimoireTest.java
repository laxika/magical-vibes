package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianGrimoireTest extends BaseCardTest {

    private void activate() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new PhyrexianGrimoire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    private PendingInteraction.GraveyardChoice activeGraveyardChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("The opponent picks one of the top two graveyard cards; it is exiled and the other goes to hand")
    void opponentChoiceExilesOnePutsOtherInHand() {
        harness.setGraveyard(player1, List.of(new LightningBolt(), new GrizzlyBears(), new Divination()));

        activate();

        PendingInteraction.GraveyardChoice choice = activeGraveyardChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        // Cards are appended to the graveyard, so the top two are the last two, topmost first.
        assertThat(choice.cardPool()).extracting("name").containsExactly("Divination", "Grizzly Bears");

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.exiledCards).extracting("card.name").containsExactly("Divination");
        assertThat(gd.playerHands.get(player1.getId())).extracting("name").containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("name").containsExactly("Lightning Bolt");
    }

    @Test
    @DisplayName("Picking the second card exiles it and the topmost card goes to hand")
    void choosingTheOtherCardSwapsTheOutcome() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));

        activate();

        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.exiledCards).extracting("card.name").containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting("name").containsExactly("Divination");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With a single card in the graveyard it is exiled and nothing goes to hand")
    void singleGraveyardCardIsExiledWithNoCardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        activate();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).extracting("card.name").containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard resolves with no choice and no card moved")
    void emptyGraveyardDoesNothing() {
        harness.setGraveyard(player1, List.of());

        activate();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent can't decline the choice")
    void graveyardChoiceIsMandatory() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));

        activate();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can't target its own controller")
    void cannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Divination()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new PhyrexianGrimoire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
