package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostfireBlade.class, GrizzlyBears.class, Ornithopter.class, SaguArcher.class})
class GhostfireBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip costs {1} when targeting a colorless creature")
    void equipColorlessCreatureCostsOne() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip still costs {3} when targeting a colored creature")
    void equipColoredCreatureCostsThree() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Paying the full equip cost moves the boost to a colored creature")
    void equipColoredCreatureMovesBoost() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("A face-down colored card receives the colorless equip discount")
    void equipFaceDownCreatureCostsOne() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new GhostfireBlade());
        harness.setHand(player1, List.of(new SaguArcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Sagu Archer");
        assertThat(creature.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
