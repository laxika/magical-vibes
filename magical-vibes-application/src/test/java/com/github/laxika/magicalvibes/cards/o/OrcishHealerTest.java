package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishHealerTest extends BaseCardTest {

    @Test
    @DisplayName("{R}{R}, {T} marks target creature so it can't be regenerated this turn")
    void preventRegeneration() {
        Permanent healer = addCreatureReady(player1, new OrcishHealer());
        Permanent skeleton = addCreatureReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, skeleton.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        assertThat(skeleton.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("{B}{B}{R}, {T} regenerates a black creature")
    void blackModeRegeneratesBlackCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent skeleton = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, skeleton.getId());
        harness.passBothPriorities();

        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("{R}{G}{G}, {T} regenerates a green creature")
    void greenModeRegeneratesGreenCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration abilities cannot target a red creature")
    void cannotRegenerateRedCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green");
    }

    @Test
    @DisplayName("Orcish Healer cannot regenerate itself — it is red")
    void cannotRegenerateItself() {
        Permanent healer = addCreatureReady(player1, new OrcishHealer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, healer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green");
    }
}
