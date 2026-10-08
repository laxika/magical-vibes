package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VerdantForce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZeroPointBallad.class, GrizzlyBears.class, HillGiant.class, VerdantForce.class})
class ZeroPointBalladTest extends BaseCardTest {

    @Test
    void destroysCreaturesWithToughnessAtMostXAndYouLoseXLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void withXSixReturnsOneDestroyedCreatureFromAnyGraveyardUnderYourControl() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(preexistingCreature));
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 6);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(bears.getCard().getId(), giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(giant.getCard()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    void doesNotReturnAcreatureWhenXIsLessThanSix() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 5);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    void cannotDeclineReturningAnEligibleCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 6);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    void xZeroLeavesPositiveToughnessCreaturesAndLifeTotalsUnchanged() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    void xSixWithNoDestroyedCreaturesDoesNotReturnPreexistingGraveyardCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 6);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 14);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    void xAboveSixCanReturnYourOwnDestroyedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveSorcery(player1, 0, 7);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 13);
    }

    @Test
    void usesCurrentToughnessIncludingCounters() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 17);
    }

    @Test
    void regeneratedCreatureSurvivesAndIsNotEligibleToReturn() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 6);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId).containsExactly(giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 14);
    }

    @Test
    void destroyedTokensAreNotEligibleCreatureCardsToReturn() {
        harness.addToBattlefield(player1, new VerdantForce());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Saproling");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ZeroPointBallad()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 6);

        harness.assertOnBattlefield(player1, "Verdant Force");
        harness.assertNotOnBattlefield(player1, "Saproling");
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId).containsExactly(giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 14);
    }
}
