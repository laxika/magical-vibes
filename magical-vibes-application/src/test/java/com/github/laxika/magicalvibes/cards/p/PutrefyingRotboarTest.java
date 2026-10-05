package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutrefyingRotboar.class, Forest.class, GrizzlyBears.class, JaceBeleren.class})
class PutrefyingRotboarTest extends BaseCardTest {

    @Test
    void boarAttackMakesNonlandHandCardCostOneLifeToCast() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void nonBoarAttackDoesNotMarkDefendingHand() {
        addCreatureReady(player1, new PutrefyingRotboar());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void eachAttackingBoarGrantsAnotherLifeLossAbility() {
        addCreatureReady(player1, new PutrefyingRotboar());
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.setLife(player2, 20);

        castHandCreature(player2);

        harness.assertLife(player2, 16);
    }

    @Test
    void grantedAbilityPersistsAfterCreatureReturnsToHand() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setLife(player2, 20);

        castHandCreature(player2);
        harness.assertLife(player2, 19);
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        castHandCreature(player2);

        harness.assertLife(player2, 18);
    }

    @Test
    void landInDefendingHandDoesNotCauseLifeLossWhenPlayed() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of(new Forest()));
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    void attackersControllerHandIsNotAffected() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setLife(player1, 20);

        castHandCreature(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    void cardEnteringHandAfterResolutionIsNotAffected() {
        addCreatureReady(player1, new PutrefyingRotboar());
        harness.setHand(player2, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        castHandCreature(player2);

        harness.assertLife(player2, 20);
    }

    @Test
    void defendingHandIsAffectedWhenAttackedPlaneswalkerLeavesBeforeResolution() {
        addCreatureReady(player1, new PutrefyingRotboar());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, jace.getId()));
            assertThat(gd.stack).isNotEmpty();
            harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, jace));
            resolveAllTriggers();
        });
        harness.setLife(player2, 20);

        castHandCreature(player2);

        harness.assertLife(player2, 19);
    }

    private void castHandCreature(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }
}
