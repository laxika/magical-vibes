package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
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

@CardUsed({PhyrexianGrimoire.class, TrainedArmodon.class})
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
    @DisplayName("Activating pays four generic mana and taps the artifact")
    void activationPaysCostAndTapsSource() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        Permanent grimoire = harness.addToBattlefieldAndReturn(player1, new PhyrexianGrimoire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(grimoire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opponent picks one of the top two graveyard cards; it is exiled and the other goes to hand")
    void opponentChoiceExilesOnePutsOtherInHand() {
        TrainedArmodon bottom = new TrainedArmodon();
        TrainedArmodon middle = new TrainedArmodon();
        TrainedArmodon top = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(bottom, middle, top));
        int initialExiledCardCount = gd.getPlayerExiledCards(player1.getId()).size();

        activate();

        PendingInteraction.GraveyardChoice choice = activeGraveyardChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        // Cards are appended to the graveyard, so the top two are the last two, topmost first.
        assertThat(choice.cardPool()).containsExactly(top, middle);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(initialExiledCardCount + 1)
                .contains(top);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(middle);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bottom);
    }

    @Test
    @DisplayName("Picking the second card exiles it and the topmost card goes to hand")
    void choosingTheOtherCardSwapsTheOutcome() {
        TrainedArmodon bottom = new TrainedArmodon();
        TrainedArmodon top = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(bottom, top));
        int initialExiledCardCount = gd.getPlayerExiledCards(player1.getId()).size();

        activate();

        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(initialExiledCardCount + 1)
                .contains(bottom);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With a single card in the graveyard it is exiled and nothing goes to hand")
    void singleGraveyardCardIsExiledWithNoCardToHand() {
        TrainedArmodon only = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(only));
        int initialExiledCardCount = gd.getPlayerExiledCards(player1.getId()).size();

        activate();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(initialExiledCardCount + 1)
                .contains(only);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard resolves with no choice and no card moved")
    void emptyGraveyardDoesNothing() {
        harness.setGraveyard(player1, List.of());
        int initialExiledCardCount = gd.getPlayerExiledCards(player1.getId()).size();

        activate();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(initialExiledCardCount);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent can't decline the choice")
    void graveyardChoiceIsMandatory() {
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new TrainedArmodon()));

        activate();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can't target its own controller")
    void cannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new TrainedArmodon()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new PhyrexianGrimoire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The top two cards are determined at resolution even if the source has left the battlefield")
    void usesCurrentGraveyardAfterSourceLeavesBattlefield() {
        TrainedArmodon bottom = new TrainedArmodon();
        TrainedArmodon formerTop = new TrainedArmodon();
        PhyrexianGrimoire grimoire = new PhyrexianGrimoire();
        TrainedArmodon opponentCard = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(bottom, formerTop));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, grimoire);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());

        // Model the source being destroyed while its activated ability is on the stack.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(bottom, formerTop, grimoire));
        harness.passBothPriorities();

        assertThat(activeGraveyardChoice().cardPool()).containsExactly(grimoire, formerTop);
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(formerTop);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(grimoire);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted opponent can choose, and an invalid answer leaves the choice available")
    void rejectedAnswersDoNotConsumeOpponentChoice() {
        TrainedArmodon bottom = new TrainedArmodon();
        TrainedArmodon top = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(bottom, top));

        activate();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(activeGraveyardChoice().cardPool()).containsExactly(top, bottom);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
