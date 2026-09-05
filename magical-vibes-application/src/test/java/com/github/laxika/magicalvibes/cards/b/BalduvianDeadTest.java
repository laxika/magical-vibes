package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Pillage;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@DisplayName("Balduvian Dead")
@CardUsed({BalduvianDead.class, StormCrow.class, Pillage.class, BazaarTrader.class})
class BalduvianDeadTest extends BaseCardTest {

    private int setUpBoard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent dead = addCreatureReady(player1, new BalduvianDead());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        return gd.playerBattlefields.get(player1.getId()).indexOf(dead);
    }

    @Test
    @DisplayName("Exiles the chosen creature card and creates a 3/1 black-and-red Graveborn with haste")
    void createsGravebornToken() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new StormCrow()));

        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Storm Crow");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Storm Crow"));

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Graveborn");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GRAVEBORN);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("The token is sacrificed at the beginning of the next end step")
    void tokenSacrificedAtNextEndStep() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new StormCrow()));

        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Graveborn");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Graveborn");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new Pillage()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate using a creature card in an opponent's graveyard")
    void cannotActivateUsingOpponentsGraveyard() {
        int idx = setUpBoard();
        harness.setGraveyard(player2, List.of(new StormCrow()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A token given to another player is not sacrificed by the delayed ability")
    void tokenGivenToAnotherPlayerIsNotSacrificed() {
        int idx = setUpBoard();
        harness.setGraveyard(player1, List.of(new StormCrow()));

        harness.activateAbility(player1, idx, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Graveborn");
        Permanent trader = addCreatureReady(player1, new BazaarTrader());
        int traderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trader);
        harness.activateAbilityWithMultiTargets(player1, traderIndex, 0, List.of(player2.getId(), token.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }
}
