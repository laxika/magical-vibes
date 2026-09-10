package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeechCollectorBloodlettingTest extends BaseCardTest {

    @Test
    @DisplayName("The first life gain each turn prepares Leech Collector only once")
    void firstLifeGainEachTurnPreparesOnlyOnce() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new LeechCollectorBloodletting());
        harness.setLife(player1, 20);

        gainLife(1);
        harness.passBothPriorities();

        assertThat(collector.isPrepared()).isTrue();
        UUID copyId = collector.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();

        gainLife(1);
        harness.passBothPriorities();

        assertThat(collector.isPrepared()).isTrue();
        assertThat(collector.getPreparedSpellCardId()).isEqualTo(copyId);
    }

    @Test
    @DisplayName("Casting Bloodletting unprepares Leech Collector and makes each opponent lose 2 life")
    void castingBloodlettingUnpreparesAndLosesLife() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new LeechCollectorBloodletting());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        gainLife(1);
        harness.passBothPriorities();

        UUID copyId = collector.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(collector.isPrepared()).isFalse();
        assertThat(collector.getPreparedSpellCardId()).isNull();
        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    private void gainLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), amount));
    }
}
