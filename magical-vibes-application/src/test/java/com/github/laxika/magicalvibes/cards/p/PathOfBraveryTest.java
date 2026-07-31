package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathOfBraveryTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures +1/+1 at the starting life total")
    void boostAtStartingLife() {
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost below the starting life total")
    void noBoostBelowStartingLife() {
        gd.playerLifeTotals.put(player1.getId(), 19);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost returns once life climbs back to the starting total")
    void boostIsDynamic() {
        gd.playerLifeTotals.put(player1.getId(), 15);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerLifeTotals.put(player1.getId(), 25);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with two creatures gains 2 life")
    void gainsLifePerAttacker() {
        harness.addToBattlefield(player1, new PathOfBravery());

        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear1.setSummoningSick(false);
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear2.setSummoningSick(false);

        gd.playerLifeTotals.put(player1.getId(), 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("No attack trigger when no creatures attack")
    void noTriggerWithoutAttackers() {
        harness.addToBattlefield(player1, new PathOfBravery());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.playerLifeTotals.put(player1.getId(), 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
