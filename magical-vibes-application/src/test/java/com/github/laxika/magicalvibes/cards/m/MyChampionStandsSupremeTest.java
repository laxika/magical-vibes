package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcanisTheOmnipotent;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyChampionStandsSupreme.class, ArcanisTheOmnipotent.class, Shock.class})
class MyChampionStandsSupremeTest extends BaseCardTest {

    @Test
    void putsTwoCountersOnYourCommanderWhenItAttacks() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        addFaceUpScheme();
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void commanderHasWardTwo() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        commander.setCommander(true);
        addFaceUpScheme();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, commander.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(commander.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commander);
    }

    @Test
    void abandonsWhenYourCommanderLeavesTheBattlefield() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        Card scheme = addFaceUpScheme();
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        commander.setCommander(true);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, commander));
        harness.runStateBasedActions();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.faceDownCommandZoneCards).contains(scheme.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme);
    }

    @Test
    void faceUpSchemeInCommandZoneAddsCountersWhenCommanderAttacks() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        addFaceUpScheme();
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPutCountersOnAnOpponentsCommanderYouControl() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player2.getId(), commanderCard);
        addFaceUpScheme();
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCountersOnYourCommanderAttackingUnderOpponentsControl() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        addFaceUpScheme();
        Permanent commander = addCreatureReady(player2, commanderCard);
        commander.setCommander(true);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void countersOpponentSpellWhenWardPaymentIsDeclined() {
        Card commanderCard = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        commander.setCommander(true);
        addFaceUpScheme();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, commander.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(commander.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    private Card addFaceUpScheme() {
        Card scheme = new MyChampionStandsSupreme();
        gd.playerCommandZones.computeIfAbsent(player1.getId(), ignored -> new ArrayList<>()).add(scheme);
        return scheme;
    }
}
