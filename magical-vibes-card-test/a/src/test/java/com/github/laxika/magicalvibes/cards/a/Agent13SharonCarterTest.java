package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Agent13SharonCarter.class, GrizzlyBears.class})
class Agent13SharonCarterTest extends BaseCardTest {

    @Test
    void investigatesWhenACreatureAttacksAlone() {
        addCreatureReady(player1, new Agent13SharonCarter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenMultipleCreaturesAttack() {
        addCreatureReady(player1, new Agent13SharonCarter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesWhenSharonAttacksAlone() {
        addCreatureReady(player1, new Agent13SharonCarter());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenOpponentAttacksAlone() {
        addCreatureReady(player1, new Agent13SharonCarter());
        addCreatureReady(player2, new Agent13SharonCarter());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenNoCreatureAttacks() {
        addCreatureReady(player1, new Agent13SharonCarter());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesEvenIfTheAttackerLeavesBeforeResolution() {
        Permanent sharon = addCreatureReady(player1, new Agent13SharonCarter());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sharon));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDrawACard() {
        addCreatureReady(player1, new Agent13SharonCarter());
        Agent13SharonCarter drawnCard = new Agent13SharonCarter();
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawnCard);
    }
}
