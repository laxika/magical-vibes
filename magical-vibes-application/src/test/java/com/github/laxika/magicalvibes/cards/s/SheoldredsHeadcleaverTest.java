package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SheoldredsHeadcleaver.class})
class SheoldredsHeadcleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic 2 gives the defending player two poison counters on combat damage")
    void toxicDealsTwoPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new SheoldredsHeadcleaver());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Menace cannot be blocked by only one creature")
    void menaceRequiresTwoBlockers() {
        Permanent attacker = addCreatureReady(player1, new SheoldredsHeadcleaver());
        harness.addToBattlefield(player2, new SheoldredsHeadcleaver());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Toxic poison is applied with combat damage without using the stack")
    void toxicIsAnImmediateDamageResult() {
        Permanent attacker = addCreatureReady(player1, new SheoldredsHeadcleaver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace allows two blockers and blocked combat gives no poison")
    void twoBlockersPreventPlayerDamageAndPoison() {
        Permanent attacker = addCreatureReady(player1, new SheoldredsHeadcleaver());
        harness.addToBattlefield(player2, new SheoldredsHeadcleaver());
        harness.addToBattlefield(player2, new SheoldredsHeadcleaver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Sheoldred's Headcleaver");
    }
}
