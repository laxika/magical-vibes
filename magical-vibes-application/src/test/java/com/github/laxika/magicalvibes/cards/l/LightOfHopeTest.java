package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightOfHope.class, Forest.class, GrizzlyBears.class, PhyrexianArena.class})
class LightOfHopeTest extends BaseCardTest {

    @Test
    void gainsFourLife() {
        castMode(0, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        castMode(1, List.of(enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    void putsPlusOnePlusOneCounterOnTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMode(2, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void destroyModeRejectsNonEnchantmentTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMode(1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    void counterModeRejectsNonCreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> castMode(2, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canDestroyOwnEnchantmentWithoutGainingLife() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PhyrexianArena());
        castMode(1, List.of(enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void canPutCounterOnOpponentsCreatureWithoutGainingLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMode(2, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void counterModeDoesNotGainLifeOrAffectAnotherCreatureWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LightOfHope);
    }

    private void castMode(int modeIndex, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new LightOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }
}
