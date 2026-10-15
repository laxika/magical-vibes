package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Triskaidekaphile.class, NoviceOccultist.class, TurnToFrog.class})
class TriskaidekaphileTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game during upkeep with exactly thirteen cards in hand")
    void winsWithExactlyThirteenCardsInHand() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(13));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during upkeep with twelve or fourteen cards in hand")
    void doesNotTriggerOutsideExactHandSize() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(12));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, cards(14));
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Checks the thirteen-card condition again on resolution")
    void conditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(13));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, cards(12));
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Draws a card for four mana")
    void activatedAbilityDrawsACard() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        Card drawnCard = new NoviceOccultist();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Has no maximum hand size")
    void hasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(9));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    private List<Card> cards(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(ignored -> new NoviceOccultist())
                .toList());
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(13));
        harness.setHand(player2, cards(13));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Drawing a fourteenth card in response prevents the win")
    void drawingInResponsePreventsWin() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(13));
        harness.setLibrary(player1, cards(2));
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(14);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Drawing the thirteenth card during upkeep does not trigger a win")
    void drawingThirteenthCardDuringUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(12));
        harness.setLibrary(player1, cards(2));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(13);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The draw ability can be activated repeatedly while tapped")
    void drawAbilityCanBeActivatedRepeatedlyWhileTapped() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards(3));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed(TurnToFrog.class)
    @DisplayName("Losing all abilities restores the hand size limit before cleanup")
    void losingAbilitiesRestoresHandSizeLimit() {
        harness.addToBattlefield(player1, new Triskaidekaphile());
        harness.setHand(player1, cards(9));
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }
}
