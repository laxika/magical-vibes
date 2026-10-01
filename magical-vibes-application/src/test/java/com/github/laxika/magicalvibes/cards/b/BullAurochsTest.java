package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BullAurochs.class, BorealDruid.class})
class BullAurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with another Aurochs gives +1/+0")
    void boostsForEachOtherAttackingAurochs() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bullAurochs.getPowerModifier()).isEqualTo(1);
        assertThat(bullAurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when attacking with two other Aurochs")
    void boostsForEachOtherAttackingAurochsTwice() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(bullAurochs.getPowerModifier()).isEqualTo(2);
        assertThat(bullAurochs.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Other attacking creatures that are not Aurochs do not count")
    void ignoresNonAurochs() {
        Permanent bullAurochs = addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BorealDruid());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bullAurochs.getPowerModifier()).isZero();
        assertThat(bullAurochs.getToughnessModifier()).isZero();
    }
}
