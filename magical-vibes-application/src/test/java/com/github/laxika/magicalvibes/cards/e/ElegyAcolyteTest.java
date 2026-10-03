package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StarbreachWhale;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElegyAcolyte.class, Forest.class, GrizzlyBears.class, StarbreachWhale.class})
class ElegyAcolyteTest extends BaseCardTest {

    @Test
    void oneOrMoreCreaturesDealingCombatDamageDrawsOneCardAndLosesOneLife() {
        addCreatureReady(player1, new ElegyAcolyte());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1, 2));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(startingHandSize + 1)
                .contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void ownCombatDamageDrawsACardAndLifelinkStillGainsLife() {
        addCreatureReady(player1, new ElegyAcolyte());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void eachAcolyteTriggersOnceForTheSameCombatDamageBatch() {
        addCreatureReady(player1, new ElegyAcolyte());
        addCreatureReady(player1, new ElegyAcolyte());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLife(player1, 20);
        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(startingHandSize + 2)
                .contains(firstDraw, secondDraw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
    }

    @Test
    void doesNotCreateRobotDuringOpponentsEndStep() {
        addCreatureReady(player1, new ElegyAcolyte());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElegyAcolyte());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void nonlandLeavingAfterEndStepBeginsDoesNotTriggerVoidRetroactively() {
        addCreatureReady(player1, new ElegyAcolyte());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElegyAcolyte());
        goToEndStep();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void voidCreatesAnUntappedColorlessTwoTwoRobotArtifactCreatureToken() {
        addCreatureReady(player1, new ElegyAcolyte());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElegyAcolyte());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        goToEndStep();

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().getColors()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        assertThat(robot.isTapped()).isFalse();
    }

    @Test
    void doesNotCreateRobotWithoutVoidEvent() {
        addCreatureReady(player1, new ElegyAcolyte());

        goToEndStep();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void createsRobotAfterNonlandPermanentLeavesBattlefield() {
        addCreatureReady(player1, new ElegyAcolyte());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        goToEndStep();

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
    }

    @Test
    void doesNotCreateRobotWhenOnlyALandLeavesBattlefield() {
        addCreatureReady(player1, new ElegyAcolyte());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        goToEndStep();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void createsRobotAfterSpellIsWarped() {
        addCreatureReady(player1, new ElegyAcolyte());
        harness.setHand(player1, List.of(new StarbreachWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        goToEndStep();

        assertThat(findPermanents(player1, "Robot")).hasSize(1);
    }

    private void goToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
