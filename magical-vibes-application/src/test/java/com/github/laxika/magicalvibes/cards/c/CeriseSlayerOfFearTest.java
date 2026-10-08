package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeriseSlayerOfFear.class, Forest.class, LlanowarElves.class, GrizzlyBears.class})
class CeriseSlayerOfFearTest extends BaseCardTest {

    @Test
    void seeksTheHighestManaValueCardWithinLifeGained() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Llanowar Elves");
    }

    @Test
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Llanowar Elves", "Grizzly Bears");
    }

    @Test
    void doesNotSeekCardAboveLifeGained() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void seeksCardWithManaValueExactlyEqualToLifeGained() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void seeksALandWhenItIsTheOnlyEligibleCard() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void seeksOnlyOneCardWhenHighestManaValuesAreTied() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
    }

    @Test
    void doesNothingWithAnEmptyLibrary() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsLifeGainDoesNotEnableTheTrigger() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player2.getId(), 2);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsSecondMainPhase() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToPostcombatMain(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void usesLifeGainedByTheTimeTheAbilityResolves() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.forceActivePlayer(player1);
        gd.mainPhasesBegunThisTurn = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotTriggerAgainInAThirdMainPhase() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.forceActivePlayer(player1);
        gd.mainPhasesBegunThisTurn = 1;
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        gd.additionalCombatMainPhasePairs = 1;
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        gd.mainPhasesBegunThisTurn = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();
    }
}
