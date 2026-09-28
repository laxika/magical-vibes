package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyChampionStandsSupreme.class, GrizzlyBears.class, Shock.class})
class MyChampionStandsSupremeTest extends BaseCardTest {

    @Test
    void putsTwoCountersOnYourCommanderWhenItAttacks() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        harness.addToBattlefield(player1, new MyChampionStandsSupreme());
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void commanderHasWardTwo() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        commander.setCommander(true);
        harness.addToBattlefield(player1, new MyChampionStandsSupreme());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, commander.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(commander.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void abandonsWhenYourCommanderLeavesTheBattlefield() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyChampionStandsSupreme());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, commanderCard);
        commander.setCommander(true);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, commander));
        harness.runStateBasedActions();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scheme.getCard());
    }
}
