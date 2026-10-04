package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnragedCeratok.class, SauroformHybrid.class, AxebaneBeast.class})
class EnragedCeratokTest extends BaseCardTest {

    @Test
    @DisplayName("Enraged Ceratok can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPower2OrLess() {
        attackingCeratok();

        addCreatureReady(player2, new SauroformHybrid());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enraged Ceratok can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPower3OrGreater() {
        attackingCeratok();

        Permanent giant = addCreatureReady(player2, new AxebaneBeast());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature boosted from power 2 to power 3 can block")
    void boostedBlockerCanBlock() {
        attackingCeratok();
        Permanent blocker = addCreatureReady(player2, new SauroformHybrid());
        blocker.setPowerModifier(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature reduced from power 3 to power 2 cannot block")
    void reducedBlockerCannotBlock() {
        attackingCeratok();
        Permanent blocker = addCreatureReady(player2, new AxebaneBeast());
        blocker.setPowerModifier(-1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with negative power cannot block Enraged Ceratok")
    void negativePowerBlockerCannotBlock() {
        attackingCeratok();
        Permanent blocker = addCreatureReady(player2, new SauroformHybrid());
        blocker.setPowerModifier(-3);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enraged Ceratok does not restrict blockers of another attacker")
    void restrictionOnlyAppliesToCeratok() {
        attackingCeratok();
        Permanent otherAttacker = addCreatureReady(player1, new AxebaneBeast());
        otherAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SauroformHybrid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingCeratok() {
        Permanent ceratok = addCreatureReady(player1, new EnragedCeratok());
        ceratok.setAttacking(true);
        return ceratok;
    }
}
