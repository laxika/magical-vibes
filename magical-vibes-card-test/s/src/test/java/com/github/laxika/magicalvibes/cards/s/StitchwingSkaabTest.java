package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StitchwingSkaab.class, Mountain.class, FieryTemper.class})
class StitchwingSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Stitchwing Skaab tapped after discarding two cards")
    void graveyardAbilityReturnsTappedAfterDiscardingTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new StitchwingSkaab()));
        harness.setHand(player1, List.of(new StitchwingSkaab(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent skaab = findPermanent(player1, "Stitchwing Skaab");
        assertThat(skaab.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Stitchwing Skaab", "Mountain");
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability with fewer than two cards in hand")
    void cannotActivateWithFewerThanTwoCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new StitchwingSkaab()));
        harness.setHand(player1, List.of(new StitchwingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Stitchwing Skaab");
    }

    @Test
    @DisplayName("Only the activated Skaab returns, during an opponent's turn")
    void returnsOnlyItsSourceDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        StitchwingSkaab source = new StitchwingSkaab();
        StitchwingSkaab other = new StitchwingSkaab();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Stitchwing Skaab");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = findPermanent(player1, "Stitchwing Skaab");
        assertThat(returned.getCard().getId()).isEqualTo(source.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(source);
    }

    @Test
    @DisplayName("An older activation cannot return a Skaab that returned and then died")
    void olderActivationDoesNotReturnNewGraveyardObject() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new StitchwingSkaab()));
        harness.setHand(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain(),
                new FieryTemper()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stitchwing Skaab");
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Stitchwing Skaab"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stitchwing Skaab");
        harness.assertNotOnBattlefield(player1, "Stitchwing Skaab");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stitchwing Skaab");
        harness.assertNotOnBattlefield(player1, "Stitchwing Skaab");
    }
}
