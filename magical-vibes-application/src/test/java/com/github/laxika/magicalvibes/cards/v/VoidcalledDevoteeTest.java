package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidcalledDevotee.class, CantorOfTheRefrain.class})
class VoidcalledDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking conjures Cantor of the Refrain into its controller's graveyard")
    void attackingConjuresCantorIntoGraveyard() {
        Permanent devotee = addCreatureReady(player1, new VoidcalledDevotee());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cantor of the Refrain");
        assertThat(devotee.isAttackedThisTurn()).isTrue();
    }

    @Test
    void normalCastCanAttackImmediatelyAndRemainsAtEndStep() {
        harness.castFromHand(player1, new VoidcalledDevotee(), "{1}{B}{B}");
        resolveAllTriggers();
        harness.assertNotInGraveyard(player1, "Cantor of the Refrain");

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cantor of the Refrain");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Voidcalled Devotee");
    }

    @Test
    void warpCastCanAttackAndConjureBeforeBeingExiled() {
        VoidcalledDevotee devotee = new VoidcalledDevotee();
        harness.setHand(player1, List.of(devotee));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cantor of the Refrain");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.findExiledCard(devotee.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Voidcalled Devotee");
        harness.assertInGraveyard(player1, "Cantor of the Refrain");
    }

    @Test
    void eachAttackerConjuresADistinctOwnedCard() {
        addCreatureReady(player1, new VoidcalledDevotee());
        addCreatureReady(player1, new VoidcalledDevotee());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(card -> {
                    assertThat(card).isInstanceOf(CantorOfTheRefrain.class);
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId()).doesNotHaveDuplicates();
        harness.assertNotInGraveyard(player2, "Cantor of the Refrain");
    }

    @Test
    void attackConjuresForControllerRatherThanDevoteesOwner() {
        VoidcalledDevotee devotee = new VoidcalledDevotee();
        devotee.setOwnerId(player1.getId());
        addCreatureReady(player2, devotee);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Cantor of the Refrain");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .singleElement().satisfies(card -> {
                    assertThat(card).isInstanceOf(CantorOfTheRefrain.class);
                    assertThat(card.getOwnerId()).isEqualTo(player2.getId());
                });
    }
}
