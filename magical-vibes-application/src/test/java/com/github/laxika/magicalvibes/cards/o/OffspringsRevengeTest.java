package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OffspringsRevenge.class, DemonOfDeathsGate.class, GrizzlyBears.class,
        HillGiant.class, Plains.class, SerraAngel.class})
class OffspringsRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Targets only red, white, or black creature cards in the controller's graveyard")
    void targetsMatchingCreatureCards() {
        HillGiant red = new HillGiant();
        SerraAngel white = new SerraAngel();
        DemonOfDeathsGate black = new DemonOfDeathsGate();
        GrizzlyBears green = new GrizzlyBears();
        Plains land = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(red, white, black, green, land)));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(red.getId(), white.getId(), black.getId());
    }

    @Test
    @DisplayName("Creates a 1/1 copy and grants it haste until the controller's next turn")
    void createsOneOneCopyWithTemporaryHaste() {
        HillGiant red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(red);

        advanceToPlayer1NextTurn();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not allow declining while a legal target exists")
    void targetIsMandatory() {
        HillGiant red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 1 cards");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToPlayer1NextTurn() {
        declareAttackers(List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
