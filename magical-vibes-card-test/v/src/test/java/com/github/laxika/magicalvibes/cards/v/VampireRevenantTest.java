package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireRevenant.class, GrizzlyBears.class, GiantSpider.class})
class VampireRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Flying Vampire Revenant cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent vampireRevenant = addCreatureReady(player1, new VampireRevenant());
        vampireRevenant.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Vampire Revenant can be blocked by another creature with flying")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player2, new VampireRevenant());
        Permanent attacker = addCreatureReady(player1, new VampireRevenant());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Vampire Revenant can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        addCreatureReady(player2, new GiantSpider());
        Permanent attacker = addCreatureReady(player1, new VampireRevenant());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Flying does not prevent Vampire Revenant from blocking a ground creature")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player2, new VampireRevenant());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
