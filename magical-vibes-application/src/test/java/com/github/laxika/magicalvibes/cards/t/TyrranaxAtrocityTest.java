package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrranaxAtrocity.class})
class TyrranaxAtrocityTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic 3 gives the defending player three poison counters")
    void toxicDealsThreePoisonCounters() {
        harness.setLife(player2, 20);
        Permanent atrocity = addCreatureReady(player1, new TyrranaxAtrocity());
        atrocity.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Toxic applies poison as combat damage is dealt without using the stack")
    void toxicAppliesDuringCombatDamage() {
        harness.setLife(player2, 20);
        Permanent atrocity = addCreatureReady(player1, new TyrranaxAtrocity());
        atrocity.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste allows Tyrranax Atrocity to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        Permanent atrocity = harness.addToBattlefieldAndReturn(player1, new TyrranaxAtrocity());

        declareAttackers(List.of(0));

        assertThat(atrocity.isTapped()).isTrue();
    }
}
