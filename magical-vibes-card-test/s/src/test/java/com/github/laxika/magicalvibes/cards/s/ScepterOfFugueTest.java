package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScepterOfFugue.class, GrizzlyBears.class, Forest.class})
class ScepterOfFugueTest extends BaseCardTest {

    private void readyScepter() {
        harness.addToBattlefield(player1, new ScepterOfFugue());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Target opponent discards a card")
    void opponentDiscards() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        readyScepter();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // The targeted player chooses which card to discard.
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target any player, including its controller")
    void controllerCanBeTargeted() {
        harness.setHand(player1, List.of(new Forest()));
        readyScepter();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Empty hand causes no discard")
    void emptyHandNoDiscard() {
        harness.setHand(player2, List.of());
        readyScepter();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new ScepterOfFugue());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeted player chooses exactly one card from a larger hand")
    void targetedPlayerChoosesOneCard() {
        harness.setHand(player2, List.of(new ScepterOfFugue(), new ScepterOfFugue()));
        var keptCard = gd.playerHands.get(player2.getId()).getFirst();
        var discardedCard = gd.playerHands.get(player2.getId()).get(1);
        readyScepter();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
    }

    @Test
    @DisplayName("Can activate during its controller's upkeep")
    void canActivateDuringOwnUpkeep() {
        harness.setHand(player2, List.of(new ScepterOfFugue()));
        readyScepter();
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Scepter of Fugue");
    }

    @Test
    @DisplayName("Activation taps the artifact and prevents another activation while tapped")
    void cannotActivateAgainWhileTapped() {
        readyScepter();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activation requires black mana even when enough generic mana is available")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new ScepterOfFugue());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
