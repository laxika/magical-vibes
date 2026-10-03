package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromokaCaptain.class, DragonScarredBear.class})
class DromokaCaptainTest extends BaseCardTest {

    @Test
    void attackingDromokaCaptainBolstersTheLeastToughCreature() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent bears = addCreatureReady(player1, new DragonScarredBear());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingAnotherCreatureDoesNotTriggerDromokaCaptain() {
        addCreatureReady(player1, new DromokaCaptain());
        Permanent bears = addCreatureReady(player1, new DragonScarredBear());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesAmongTiedCreaturesWithoutIncludingOpponentCreatures() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent other = addCreatureReady(player1, new DromokaCaptain());
        Permanent opponent = addCreatureReady(player2, new DromokaCaptain());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(captain.getId(), other.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(other.getId()));

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesCurrentToughnessWhenTheTriggerResolves() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());

        declareAttackers(List.of(0));
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerStillBolstersAfterCaptainLeavesTheBattlefield() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        gd.playerGraveyards.get(player1.getId()).add(captain.getCard());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerDoesNothingWhenNoControlledCreatureRemains() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent opponent = addCreatureReady(player2, new DragonScarredBear());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        gd.playerGraveyards.get(player1.getId()).add(captain.getCard());
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bolsterLetsCaptainKillABlockerBeforeItDealsDamage() {
        Permanent captain = addCreatureReady(player1, new DromokaCaptain());
        Permanent blocker = addCreatureReady(player2, new DragonScarredBear());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
}
