package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.cards.p.PhantomWings;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishInfantry.class, CloudDjinn.class, DuskriderFalcon.class, PhantomWings.class})
class CloudDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud Djinn can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent djinn = addCreatureReady(player2, new CloudDjinn());

        Permanent attacker = addCreatureReady(player1, new DuskriderFalcon());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(djinn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Djinn cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new CloudDjinn());

        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Cloud Djinn can block a creature granted flying by an Aura")
    void canBlockCreatureGrantedFlying() {
        Permanent djinn = addCreatureReady(player2, new CloudDjinn());
        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new PhantomWings());
        wings.setAttachedTo(attacker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(djinn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Cloud Djinn")
    void nonFlyingCreatureCannotBlockDjinn() {
        addCreatureReady(player1, new CloudDjinn());
        addCreatureReady(player2, new BenalishInfantry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cloud Djinn is unaffected when attacking")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent djinn = addCreatureReady(player1, new CloudDjinn());
        djinn.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
