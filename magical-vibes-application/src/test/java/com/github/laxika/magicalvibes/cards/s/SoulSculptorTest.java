package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a target creature into an enchantment without abilities")
    void turnsTargetCreatureIntoEnchantment() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, elves)).isFalse();
        assertThat(gqs.isEnchantment(gd, elves)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, elves).losesAllAbilities()).isTrue();
    }

    @Test
    @DisplayName("The effect ends when any player casts a creature spell")
    void endsWhenAnyPlayerCastsCreatureSpell() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gqs.isCreature(gd, elves)).isTrue();
        assertThat(gqs.isEnchantment(gd, elves)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, elves).losesAllAbilities()).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void canTargetOnlyCreature() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
