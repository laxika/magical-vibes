package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YarusRoarOfTheOldGods.class, GrizzlyBears.class, Divination.class, Island.class,
        SanitationAutomaton.class})
class YarusRoarOfTheOldGodsTest extends BaseCardTest {

    @Test
    void otherCreaturesYouControlHaveHaste() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void multipleFaceDownCreaturesDealDamageAndDrawOnlyOnce() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        addCreatureReady(player1, new YarusRoarOfTheOldGods());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        secondAttacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackers(List.of(1, 2));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void faceDownCreatureDiesAndReturnsToItsOwnersBattlefieldFaceUp() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Card bearCard = new GrizzlyBears();
        bearCard.setOwnerId(player2.getId());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, bearCard);
        gd.stolenCreatures.put(bear.getId(), player2.getId());
        bear.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        bear.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent ->
                permanent.getCard().getId().equals(bearCard.getId()));
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(bearCard.getId());
        assertThat(returned.isFaceDown()).isFalse();
    }

    @Test
    void faceDownNonPermanentCardDoesNotReturn() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent divination = harness.addToBattlefieldAndReturn(player1, new Divination());
        divination.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        divination.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void faceDownNonCreaturePermanentReturnsAndTurnsFaceUp() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        island.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Island");
        assertThat(returned.getCard().getId()).isEqualTo(island.getCard().getId());
        assertThat(returned.isFaceDown()).isFalse();
    }

    @Test
    void yarusDoesNotGiveItselfHaste() {
        Permanent yarus = harness.addToBattlefieldAndReturn(player1, new YarusRoarOfTheOldGods());

        assertThat(als.canAttack(gd, yarus, player1.getId())).isFalse();
    }

    @Test
    void faceUpCombatDamageDoesNotDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        addCreatureReady(player1, new YarusRoarOfTheOldGods());
        addCreatureReady(player1, new SanitationAutomaton());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void faceUpCreatureDeathDoesNotReturn() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        automaton.setMarkedDamage(1);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sanitation Automaton");
        harness.assertInGraveyard(player1, "Sanitation Automaton");
    }

    @Test
    void opponentsFaceDownCreatureDeathDoesNotReturn() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        automaton.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        automaton.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Sanitation Automaton");
        harness.assertInGraveyard(player2, "Sanitation Automaton");
    }

    @Test
    void simultaneousDeathOfYarusStillReturnsEachFaceDownCreature() {
        Permanent yarus = harness.addToBattlefieldAndReturn(player1, new YarusRoarOfTheOldGods());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        first.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        second.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        yarus.setMarkedDamage(4);
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Yarus, Roar of the Old Gods");
        assertThat(findPermanents(player1, "Sanitation Automaton")).hasSize(2)
                .allMatch(permanent -> !permanent.isFaceDown());
    }

    @Test
    void returningFaceDownDoesNotTriggerPrintedEnterAbility() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        automaton.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        automaton.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sanitation Automaton").isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cardRemovedFromGraveyardBeforeResolutionDoesNotReturn() {
        harness.addToBattlefield(player1, new YarusRoarOfTheOldGods());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        island.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Island");
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(island.getCard()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Island");
    }
}
