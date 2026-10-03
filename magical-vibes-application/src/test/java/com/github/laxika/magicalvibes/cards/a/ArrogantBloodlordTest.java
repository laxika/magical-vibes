package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArrogantBloodlord.class, CaravanEscort.class, GlorySeeker.class})
class ArrogantBloodlordTest extends BaseCardTest {

    @Test
    void becomesBlockedByPowerOneCreatureSchedulesSelfDestruction() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        addCreatureReady(player2, new CaravanEscort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }

    @Test
    void blocksPowerOneCreatureSchedulesSelfDestruction() {
        Permanent escort = addCreatureReady(player1, new CaravanEscort());
        escort.setAttacking(true);
        Permanent bloodlord = addCreatureReady(player2, new ArrogantBloodlord());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }

    @Test
    void doesNotTriggerForPowerTwoCreature() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ArrogantBloodlord());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    void delayedDestructionTriggersOnStackAsEndOfCombatBegins() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        addCreatureReady(player2, new CaravanEscort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bloodlord);
        assertThat(gd.stack).anyMatch(entry -> bloodlord.getId().equals(entry.getSourcePermanentId()));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bloodlord);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ArrogantBloodlord);
    }

    @Test
    void powerIncreaseAfterBlockingDoesNotPreventDelayedDestruction() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        Permanent escort = addCreatureReady(player2, new CaravanEscort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        escort.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }

    @Test
    void becomesBlockedByPowerTwoCreatureDoesNotTrigger() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        addCreatureReady(player2, new GlorySeeker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    void eachQualifyingBlockerTriggersSeparately() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        addCreatureReady(player2, new CaravanEscort());
        addCreatureReady(player2, new CaravanEscort());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(entry -> bloodlord.getId().equals(entry.getSourcePermanentId()))
                .hasSize(2);
    }

    @Test
    void zeroPowerBlockerAlsoTriggers() {
        Permanent bloodlord = addCreatureReady(player1, new ArrogantBloodlord());
        bloodlord.setAttacking(true);
        Permanent escort = addCreatureReady(player2, new CaravanEscort());
        escort.setPowerModifier(-1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(bloodlord.getId()));
    }
}
