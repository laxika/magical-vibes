package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SeagrafSkaab;
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

@DisplayName("Geralf's Masterpiece")
@CardUsed({GeralfsMasterpiece.class, Island.class, Mountain.class, SeagrafSkaab.class})
class GeralfsMasterpieceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -1/-1 for each card in its controller's hand")
    void getsMinusOneForEachCardInHand() {
        harness.setHand(player1, List.of(new SeagrafSkaab(), new Mountain()));
        harness.addToBattlefield(player1, new GeralfsMasterpiece());

        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");

        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(5);
    }

    @Test
    @DisplayName("Returns tapped from the graveyard after discarding three cards")
    void returnsTappedAfterDiscardingThreeCards() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new SeagrafSkaab(), new Mountain(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");

        assertThat(masterpiece.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(7);
        harness.assertNotInGraveyard(player1, "Geralf's Masterpiece");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without three cards in hand")
    void cannotActivateWithoutThreeCardsInHand() {
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new SeagrafSkaab(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Geralf's Masterpiece");
    }

    @Test
    @DisplayName("Hand-size penalty updates continuously and ignores the opponent's hand")
    void penaltyTracksOnlyControllersCurrentHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island(), new Mountain(), new Island()));
        harness.addToBattlefield(player1, new GeralfsMasterpiece());
        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");

        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(7);

        harness.setHand(player1, List.of(new Island(), new Mountain(), new Island()));
        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(4);

        harness.setHand(player1, List.of(new Mountain()));
        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(6);
    }

    @Test
    @DisplayName("Pays exactly three discards before resolution and returns only the activated copy")
    void returnsOnlySourceAndKeepsRemainingHandCard() {
        GeralfsMasterpiece source = new GeralfsMasterpiece();
        GeralfsMasterpiece other = new GeralfsMasterpiece();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, List.of(new Island(), new Island(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Geralf's Masterpiece");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source, other).hasSize(5);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Geralf's Masterpiece")).hasSize(1);
        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");
        assertThat(masterpiece.getCard().getId()).isEqualTo(source.getId());
        assertThat(masterpiece.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(source).hasSize(4);
    }

    @Test
    @DisplayName("Can return during an opponent's combat step")
    void canActivateOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new Island(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Geralf's Masterpiece").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Geralf's Masterpiece");
    }

    @Test
    @DisplayName("Cannot pay the blue mana requirement with colorless mana")
    void cannotActivateWithoutBlueMana() {
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new Island(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Geralf's Masterpiece");
        harness.assertNotOnBattlefield(player1, "Geralf's Masterpiece");
    }
}
