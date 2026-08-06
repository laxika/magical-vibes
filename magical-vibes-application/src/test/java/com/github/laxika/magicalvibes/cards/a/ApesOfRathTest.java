package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApesOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pushes a triggered ability sourced from Apes of Rath")
    void attackTriggerPushesOntoStack() {
        Permanent apes = addReadyApes(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(apes.getId());
    }

    @Test
    @DisplayName("Resolving the attack trigger marks Apes of Rath to skip its next untap step")
    void resolvingSkipsNextUntapOnSelf() {
        Permanent apes = addReadyApes(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(apes.isTapped()).isTrue();
        assertThat(apes.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Apes of Rath is unmarked while it hasn't attacked")
    void noSkipUntapWithoutAttacking() {
        Permanent apes = addReadyApes(player1);

        assertThat(apes.getSkipUntapCount()).isZero();
    }

    private Permanent addReadyApes(Player player) {
        Permanent perm = new Permanent(new ApesOfRath());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
