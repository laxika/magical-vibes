package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattlewandOakTest extends BaseCardTest {

    private Permanent addOak() {
        harness.addToBattlefield(player1, new BattlewandOak());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +2/+2 when a Forest you control enters")
    void pumpsWhenForestEnters() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not pump when a non-Forest land enters")
    void noPumpForNonForestLand() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new Island()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+2 when you cast a Treefolk spell")
    void pumpsWhenTreefolkCast() {
        Permanent oak = addOak();

        // A second Battlewand Oak is a Treefolk spell.
        harness.setHand(player1, List.of(new BattlewandOak()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        // Cast trigger sits on the stack above the creature spell.
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not pump when you cast a non-Treefolk spell")
    void noPumpForNonTreefolkSpell() {
        Permanent oak = addOak();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // No cast trigger — only the creature spell is on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }
}
