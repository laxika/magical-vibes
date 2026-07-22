package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavageAllianceTest extends BaseCardTest {

    // Modes: 0 = trample to target player's creatures, 1 = 2 damage to creature,
    // 2 = 1 damage to each creature target opponent controls

    @Test
    @DisplayName("Trample mode: creatures target player controls gain trample until end of turn")
    void trampleModeGrantsTrample() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opp = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageAlliance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opp, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature mode: deals 2 damage to target creature")
    void creatureModeDealsTwoDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageAlliance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{1}, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent mass mode: deals 1 damage to each creature target opponent controls")
    void opponentMassModeDamagesEachCreature() {
        Permanent a = addCreatureReady(player2, new GrizzlyBears());
        Permanent b = addCreatureReady(player2, new GrizzlyBears());
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageAlliance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(a.getMarkedDamage()).isEqualTo(1);
        assertThat(b.getMarkedDamage()).isEqualTo(1);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Two modes: escalate {1} and both effects resolve")
    void twoModesEscalateAndResolve() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageAlliance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0, 1},
                List.of(player1.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isTrue();
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Two modes without escalate mana is rejected")
    void twoModesWithoutEscalateManaRejected() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SavageAlliance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0, 1},
                        List.of(player1.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
