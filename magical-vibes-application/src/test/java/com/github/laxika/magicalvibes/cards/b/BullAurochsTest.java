package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Aurochs;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BullAurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with another Aurochs gives +1/+0")
    void boostsForEachOtherAttackingAurochs() {
        Permanent bullAurochs = addCreatureReady(new BullAurochs());
        addCreatureReady(new Aurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bullAurochs.getPowerModifier()).isEqualTo(1);
        assertThat(bullAurochs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Other attacking creatures that are not Aurochs do not count")
    void ignoresNonAurochs() {
        Permanent bullAurochs = addCreatureReady(new BullAurochs());
        addCreatureReady(new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bullAurochs.getPowerModifier()).isZero();
        assertThat(bullAurochs.getToughnessModifier()).isZero();
    }

    private Permanent addCreatureReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
