package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintWeaver.class, GrizzlyBears.class, HillGiant.class})
class GlintWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes counters before gaining life from the greatest toughness")
    void distributesCountersThenGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(target.getId(), 3);
        harness.setLife(player1, 20);

        castGlintWeaver();
        resolveGlintWeaver();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Life gain counts only creatures controlled by the Glint Weaver's controller")
    void lifeGainExcludesOpponentsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.pendingETBDamageAssignments = Map.of(opponentTarget.getId(), 3);
        harness.setLife(player1, 20);

        castGlintWeaver();
        resolveGlintWeaver();

        assertThat(opponentTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    private void castGlintWeaver() {
        harness.setHand(player1, List.of(new GlintWeaver()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveGlintWeaver() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
