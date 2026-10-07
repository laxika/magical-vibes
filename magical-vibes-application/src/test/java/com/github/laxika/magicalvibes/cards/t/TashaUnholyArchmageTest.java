package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mordenkainen;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TashaUnholyArchmage.class, GrizzlyBears.class, Shock.class, Forest.class, Mordenkainen.class})
class TashaUnholyArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts -1/-1 counters on creatures attacking you or Tasha")
    void plusOneTriggersForPlayerAndPlaneswalkerAttacks() {
        Permanent tasha = addReadyTasha(4);
        Permanent directAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalkerAttacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int directIndex = gd.playerBattlefields.get(player2.getId()).indexOf(directAttacker);
        int planeswalkerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(planeswalkerAttacker);
        gs.declareAttackers(gd, player2, List.of(directIndex, planeswalkerIndex),
                Map.of(directIndex, player1.getId(), planeswalkerIndex, tasha.getId()));
        resolveAllTriggers();

        assertThat(directAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(planeswalkerAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 returns an opponent's creature with ward {2}")
    void minusTwoReturnsCreatureWithWard() {
        addReadyTasha(4);
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creatureCard));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.WARD)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("-6 puts three revealed creatures onto your battlefield and the rest into the opponent's graveyard")
    void minusSixRevealsThreeCreaturesAndGraveyardsTheRest() {
        addReadyTasha(6);
        Card shock = new Shock();
        Card firstCreature = new GrizzlyBears();
        Card forest = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondShock = new Shock();
        Card thirdCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(shock, firstCreature, forest, secondCreature, secondShock, thirdCreature));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(shock, forest, secondShock);
    }

    @Test
    void plusOneDoesNotTriggerForAnotherPlaneswalker() {
        addReadyTasha(4);
        Permanent otherPlaneswalker = harness.addToBattlefieldAndReturn(player1, new Mordenkainen());
        otherPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, otherPlaneswalker.getId()));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void minusTwoLetsOpponentChooseAmongCreatures() {
        addReadyTasha(4);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land, first, second));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player2, 1);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard()).isSameAs(second);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.WARD)).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land, first);
    }

    @Test
    void minusTwoDoesNothingWithoutCreatureCards() {
        addReadyTasha(4);
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusSixStopsAtThirdCreatureLeavingUnrevealedCardsInLibrary() {
        addReadyTasha(7);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card unrevealed = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, unrevealed));
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(unrevealed);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void minusSixReturnsAvailableCreaturesWhenLibraryHasFewerThanThree() {
        addReadyTasha(7);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land, creature));
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCard()).isSameAs(creature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
    }

    private Permanent addReadyTasha(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TashaUnholyArchmage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
