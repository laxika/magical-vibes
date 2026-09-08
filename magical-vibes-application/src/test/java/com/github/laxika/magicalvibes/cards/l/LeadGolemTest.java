package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LeadGolem.class)
class LeadGolemTest extends BaseCardTest {

    // ===== Attack trigger pushes onto stack =====

    @Test
    @DisplayName("Attacking with Lead Golem pushes a triggered ability sourced from itself")
    void attackTriggerPushesOntoStack() {
        Permanent golem = addCreatureReady(player1, new LeadGolem());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(golem.getId());
    }

    // ===== Attack trigger resolution =====

    @Test
    @DisplayName("Resolving the attack trigger sets skipUntapCount on Lead Golem itself")
    void resolvingSetsSkipUntapCountOnSelf() {
        Permanent golem = addCreatureReady(player1, new LeadGolem());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(golem.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving the trigger logs that Lead Golem won't untap")
    void resolvingLogsSkipUntap() {
        addCreatureReady(player1, new LeadGolem());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Lead Golem") && log.contains("untap"));
    }

    @Test
    @DisplayName("Attacking keeps Lead Golem tapped through its next untap step only")
    void attackSkipsNextUntapOnly() {
        Permanent golem = addCreatureReady(player1, new LeadGolem());
        Permanent otherGolem = addCreatureReady(player1, new LeadGolem());
        otherGolem.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(golem.isTapped()).isTrue();
        assertThat(golem.getSkipUntapCount()).isZero();
        assertThat(otherGolem.isTapped()).isFalse();

        harness.performUntapStep(player1);

        assertThat(golem.isTapped()).isFalse();
    }
}
