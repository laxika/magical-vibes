package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolcanicSalvoTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the total power of creatures you control")
    void costReductionUsesControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new VolcanicSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires the full cost when no creatures are controlled")
    void requiresFullCostWithoutCreatures() {
        harness.setHand(player1, List.of(new VolcanicSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not use an opponent's creature power for the reduction")
    void opponentCreaturePowerDoesNotReduceCost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Deals 6 damage to two target creatures")
    void damagesTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new VolcanicSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("May target only creatures or planeswalkers")
    void rejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VolcanicSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
