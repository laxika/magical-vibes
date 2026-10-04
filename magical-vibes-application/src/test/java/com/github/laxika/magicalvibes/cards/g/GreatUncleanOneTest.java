package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatUncleanOne.class})
class GreatUncleanOneTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, each opponent loses 2 life and a lower-life opponent creates a Plaguebearer")
    void losesLifeThenCreatesTokenForLowerLifeOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        runToEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Great Unclean One", "Plaguebearer of Nurgle");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(countPermanents(player2, "Plaguebearer of Nurgle")).isZero();

        var token = findPermanent(player1, "Plaguebearer of Nurgle");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not create a token for an opponent who is not lower life after the loss")
    void noTokenForOpponentStillAtLeastAsMuchLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        runToEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Great Unclean One");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        enterEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Plaguebearer of Nurgle")).isZero();
    }

    @Test
    @DisplayName("An opponent still above your life total loses life but produces no token")
    void noTokenForOpponentWithMoreLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 23);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        runToEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(countPermanents(player1, "Plaguebearer of Nurgle")).isZero();
    }

    @Test
    @DisplayName("Compares current life totals on resolution rather than when the ability triggers")
    void comparesLifeTotalsOnResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GreatUncleanOne());
        enterEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 17);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Plaguebearer of Nurgle")).isZero();
    }

    @Test
    @DisplayName("Multiple copies compare life totals separately after each ability's life loss")
    void multipleCopiesResolveIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.addToBattlefield(player1, new GreatUncleanOne());
        harness.addToBattlefield(player1, new GreatUncleanOne());
        enterEndStep(player1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Plaguebearer of Nurgle")).isZero();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Plaguebearer of Nurgle")).isEqualTo(1);
    }

    private void enterEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void runToEndStep() {
        enterEndStep(player1);
        harness.passBothPriorities();
    }
}
