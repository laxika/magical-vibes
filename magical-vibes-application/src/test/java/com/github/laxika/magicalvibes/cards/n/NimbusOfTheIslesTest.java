package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({NimbusOfTheIsles.class, GrizzlyBears.class, NetcasterSpider.class})
class NimbusOfTheIslesTest extends BaseCardTest {

    @Test
    @DisplayName("Flying Nimbus of the Isles cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NimbusOfTheIsles());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Nimbus of the Isles can be blocked by another creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NimbusOfTheIsles());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new NimbusOfTheIsles());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Nimbus of the Isles can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NimbusOfTheIsles());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new NetcasterSpider());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
