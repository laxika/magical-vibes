package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BardsCompany.class, EliteVanguard.class, Forest.class, GrizzlyBears.class})
class BardsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast at instant speed while controlling a Human")
    void canCastWithHumanDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new EliteVanguard());
        prepareOpponentTurn();
        harness.setHand(player1, List.of(new BardsCompany()));
        addBardsMana();

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without a Human")
    void cannotCastWithoutHumanDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareOpponentTurn();
        harness.setHand(player1, List.of(new BardsCompany()));
        addBardsMana();

        harness.getGameService().passPriority(harness.getGameData(), player2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void boostsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new BardsCompany());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering recruits after discarding a nonland card")
    void enteringRecruitsAfterNonlandDiscard() {
        prepareRecruit(new GrizzlyBears(), new Forest());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking recruits after discarding a nonland card")
    void attackingRecruitsAfterNonlandDiscard() {
        addCreatureReady(player1, new BardsCompany());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Recruit creates its token during the same resolution as the discard")
    void recruitCreatesTokenWithoutAnotherPriorityRound() {
        prepareRecruit(new BardsCompany(), new Forest());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discarding a land recruits without creating a token")
    void landDiscardDoesNotCreateToken() {
        prepareRecruit(new Forest(), new BardsCompany());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Bard's Company");
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Recruit draws before discarding when the hand starts empty")
    void recruitDiscardsNewlyDrawnCardFromEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BardsCompany()));
        harness.enterBattlefieldAndReturn(player1, new BardsCompany());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Bard's Company");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Bard's Company does not boost itself, but two copies boost each other")
    void companiesBoostEachOtherButNotThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BardsCompany());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new BardsCompany());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's Human does not grant flash permission")
    void opponentsHumanDoesNotPermitInstantSpeedCasting() {
        harness.addToBattlefield(player2, new BardsCompany());
        prepareOpponentTurn();
        harness.setHand(player1, List.of(new BardsCompany()));
        addBardsMana();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareRecruit(Card discardedCard, Card drawnCard) {
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new BardsCompany());
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addBardsMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
