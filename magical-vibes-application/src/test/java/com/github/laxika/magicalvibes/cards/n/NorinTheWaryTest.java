package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.Sprout;
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

@CardUsed({NorinTheWary.class, Sprout.class, AshcoatBear.class})
class NorinTheWaryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles when any player casts a spell and returns at the next end step")
    void exilesWhenAnyPlayerCastsSpell() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sprout()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertExiled(norin);

        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertReturned(norin);
    }

    @Test
    @DisplayName("Exiles when its controller casts a spell and returns at the next end step")
    void exilesWhenItsControllerCastsSpell() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Sprout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertExiled(norin);

        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertReturned(norin);
    }

    @Test
    @DisplayName("Returns to its owner's battlefield after being controlled by another player")
    void returnsUnderOwnersControl() {
        Card norinCard = new NorinTheWary();
        norinCard.setOwnerId(player1.getId());
        Permanent norin = harness.addToBattlefieldAndReturn(player2, norinCard);
        gd.stolenCreatures.put(norin.getId(), player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Sprout()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertExiled(norin);

        harness.passBothPriorities();
        advanceToEndStep(player2);

        assertReturned(norin);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(norin.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles when any creature attacks and returns at the next end step")
    void exilesWhenAnyCreatureAttacks() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());
        addCreatureReady(player2, new AshcoatBear());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertExiled(norin);

        advanceToEndStep(player2);

        assertReturned(norin);
    }

    @Test
    @DisplayName("Exiles before a creature spell resolves")
    void exilesWhenCreatureSpellIsCast() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertExiled(norin);
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        advanceToEndStep(player1);
        assertReturned(norin);
    }

    @Test
    @DisplayName("Exiles when Norin itself attacks before dealing combat damage")
    void exilesWhenItAttacks() {
        Permanent norin = addCreatureReady(player1, new NorinTheWary());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertExiled(norin);
        advanceToEndStep(player1);
        harness.assertLife(player2, 20);
        assertReturned(norin);
        Permanent returned = findPermanent(player1, "Norin the Wary");
        assertThat(returned.getId()).isNotEqualTo(norin.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A spell cast during the end step delays Norin's return until the following end step")
    void exileDuringEndStepReturnsNextTurn() {
        Permanent norin = harness.addToBattlefieldAndReturn(player1, new NorinTheWary());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Sprout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();
        assertExiled(norin);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertExiled(norin);
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertExiled(norin);

        resolveAllTriggers();
        assertReturned(norin);
    }

    private void assertExiled(Permanent norin) {
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(norin);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(norin.getCard().getId()));
    }

    private void assertReturned(Permanent norin) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(norin.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(norin.getCard().getId()));
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.passUntilWithNoAttackers(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
