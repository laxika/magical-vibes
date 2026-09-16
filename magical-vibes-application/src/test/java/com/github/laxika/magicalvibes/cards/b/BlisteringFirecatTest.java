package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlisteringFirecat.class, GlorySeeker.class})
class BlisteringFirecatTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyBecauseOfHaste() {
        harness.setLife(player2, 20);

        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new BlisteringFirecat());
        firecat.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 13);
    }

    @Test
    void assignsTrampleDamageToTheDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent firecat = addCreatureReady(player1, new BlisteringFirecat());
        firecat.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 5
        ));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Blistering Firecat");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    void sacrificesAtTheBeginningOfTheEndStepWhenCastNormally() {
        harness.castFromHand(player1, new BlisteringFirecat(), "{1}{R}{R}{R}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blistering Firecat");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blistering Firecat");
        harness.assertInGraveyard(player1, "Blistering Firecat");
    }

    @Test
    void sacrificesAtTheBeginningOfAnOpponentsEndStep() {
        addCreatureReady(player1, new BlisteringFirecat());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blistering Firecat");
        harness.assertInGraveyard(player1, "Blistering Firecat");
    }

    @Test
    void doesNotTriggerWhileFaceDown() {
        harness.setHand(player1, List.of(new BlisteringFirecat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent firecat = findPermanent(player1, "Blistering Firecat");
        assertThat(firecat.isFaceDown()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Blistering Firecat");
        harness.assertNotInGraveyard(player1, "Blistering Firecat");
    }

    @Test
    void sacrificesAtTheBeginningOfTheEndStepAfterBeingTurnedFaceUp() {
        harness.setHand(player1, List.of(new BlisteringFirecat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent firecat = findPermanent(player1, "Blistering Firecat");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firecat));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blistering Firecat");
        harness.assertInGraveyard(player1, "Blistering Firecat");
    }
}
