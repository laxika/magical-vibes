package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ApesOfRath.class)
class ApesOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pushes a triggered ability sourced from Apes of Rath")
    void attackTriggerPushesOntoStack() {
        Permanent apes = addCreatureReady(player1, new ApesOfRath());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(apes.getId());
    }

    @Test
    @DisplayName("Resolving the attack trigger marks Apes of Rath to skip its next untap step")
    void resolvingSkipsNextUntapOnSelf() {
        Permanent apes = addCreatureReady(player1, new ApesOfRath());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(apes.isTapped()).isTrue();
        assertThat(apes.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Apes of Rath is unmarked while it hasn't attacked")
    void noSkipUntapWithoutAttacking() {
        Permanent apes = addCreatureReady(player1, new ApesOfRath());

        assertThat(apes.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Attacking keeps Apes of Rath tapped through its next untap step only")
    void attackSkipsNextUntapOnly() {
        Permanent apes = addCreatureReady(player1, new ApesOfRath());
        Permanent otherApes = addCreatureReady(player1, new ApesOfRath());
        otherApes.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(apes.isTapped()).isTrue();
        assertThat(apes.getSkipUntapCount()).isZero();
        assertThat(otherApes.isTapped()).isFalse();

        harness.performUntapStep(player1);

        assertThat(apes.isTapped()).isFalse();
    }
}
