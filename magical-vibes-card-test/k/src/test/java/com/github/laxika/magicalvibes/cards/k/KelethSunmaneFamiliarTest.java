package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LosheelClockworkScholar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KelethSunmaneFamiliar.class, GrizzlyBears.class, LosheelClockworkScholar.class})
class KelethSunmaneFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a commander that attacks")
    void commanderAttacks() {
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a noncommander attacker")
    void noncommanderAttacks() {
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersForItselfWhenItIsACommander() {
        Card commanderCard = new KelethSunmaneFamiliar();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsCommander() {
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = addCreatureReady(player2, commanderCard);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersForAnOpponentsCommanderYouControl() {
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnCommanderEvenIfItChangesControlBeforeResolution() {
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerBattlefields.get(player2.getId()).add(commander);
        commander.setAttacking(false);
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void resolvesEvenIfKelethLeavesTheBattlefield() {
        Permanent keleth = addCreatureReady(player1, new KelethSunmaneFamiliar());
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(keleth);
        gd.playerGraveyards.get(player1.getId()).add(keleth.getCard());
        resolveAllTriggers();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterOnAnotherCreatureIfCommanderLeavesTheBattlefield() {
        Permanent keleth = addCreatureReady(player1, new KelethSunmaneFamiliar());
        Card commanderCard = new LosheelClockworkScholar();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerGraveyards.get(player1.getId()).add(commanderCard);
        resolveAllTriggers();

        assertThat(keleth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
