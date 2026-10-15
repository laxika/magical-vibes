package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gleancrawler.class, ElvesOfDeepShadow.class, LastGasp.class, BorosSignet.class, Putrefy.class})
class GleancrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature cards that were put into your graveyard from the battlefield this turn")
    void returnsCreaturesPutIntoGraveyardFromBattlefieldThisTurn() {
        Card alreadyInGraveyard = new ElvesOfDeepShadow();
        Card diedThisTurn = new ElvesOfDeepShadow();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedThisTurn);

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(alreadyInGraveyard.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Card diedThisTurn = new ElvesOfDeepShadow();
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedThisTurn);

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedThisTurn.getId()));
    }

    @Test
    @DisplayName("Does not return creatures put into your graveyard during a previous turn")
    void doesNotReturnCreaturesFromPreviousTurn() {
        Card diedLastTurn = new ElvesOfDeepShadow();
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, diedLastTurn);

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(diedLastTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedLastTurn.getId()));
    }

    @Test
    @DisplayName("Returns all own creature cards only, excluding noncreatures and opponents' cards")
    void returnsAllOwnCreatureCardsOnly() {
        Card firstCreature = new ElvesOfDeepShadow();
        Card secondCreature = new ElvesOfDeepShadow();
        Card nonCreature = new BorosSignet();
        Card opponentCreature = new ElvesOfDeepShadow();

        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, firstCreature);
        harness.addToBattlefield(player1, secondCreature);
        harness.addToBattlefield(player1, nonCreature);
        harness.addToBattlefield(player2, opponentCreature);

        harness.setHand(player1, List.of(new Putrefy(), new Putrefy(), new LastGasp(), new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boros Signet"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Elves of Deep Shadow"));

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firstCreature.getId()))
                .anyMatch(card -> card.getId().equals(secondCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonCreature.getId()))
                .noneMatch(card -> card.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Returns creatures that died before Gleancrawler entered the battlefield")
    void returnsCreaturesThatDiedBeforeEntering() {
        Card creature = new ElvesOfDeepShadow();
        harness.addToBattlefield(player1, creature);
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));
        harness.addToBattlefield(player1, new Gleancrawler());

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Returns a creature that dies in response even when the graveyard was initially empty")
    void returnsCreatureThatDiesInResponse() {
        Card creature = new ElvesOfDeepShadow();
        harness.addToBattlefield(player1, new Gleancrawler());
        harness.addToBattlefield(player1, creature);
        harness.setHand(player1, List.of(new LastGasp()));
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Elves of Deep Shadow"));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Returns Gleancrawler itself when it dies in response to its trigger")
    void returnsItselfWhenDestroyedInResponse() {
        Card gleancrawler = new Gleancrawler();
        harness.addToBattlefield(player1, gleancrawler);
        harness.setHand(player1, List.of(new Putrefy()));
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gleancrawler"));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gleancrawler);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(gleancrawler);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gleancrawler);
    }

    private void advanceToEndStep(Player activePlayer) {
        beginEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
