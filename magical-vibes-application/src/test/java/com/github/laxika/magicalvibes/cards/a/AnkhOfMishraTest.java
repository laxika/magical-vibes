package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnkhOfMishra.class, Forest.class, Mountain.class, GrizzlyBears.class, WordOfSeizing.class})
class AnkhOfMishraTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's land entering deals 2 damage to that opponent")
    void opponentLandDamagesOpponent() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve Ankh trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller's own land entering deals 2 damage to the controller")
    void ownLandDamagesController() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Ankh of Mishra trigger separately, dealing 2 damage each")
    void twoAnkhsStack() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A land put onto the battlefield without being played triggers Ankh of Mishra")
    void landPutOntoBattlefieldTriggers() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Uses the land's controller when the trigger resolves")
    void usesLandControllerAtResolution() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent land = harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new WordOfSeizing()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, land.getId());

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(land.getId()));

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Uses the new controller when your land's trigger resolves")
    void ownLandUsesNewControllerAtResolution() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent land = harness.enterBattlefieldAndReturn(player1, new Mountain());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, land.getId());

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(land.getId()));

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A non-land entering does not trigger Ankh of Mishra")
    void nonLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new AnkhOfMishra());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
