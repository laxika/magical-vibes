package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.e.ElvishPioneer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrasslandCrusader.class, ElvishPioneer.class, GlorySeeker.class, BarkhideMauler.class})
class GrasslandCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a target Elf +2/+2 until end of turn")
    void boostsTargetElf() {
        Permanent crusader = addCreatureReady(player1, new GrasslandCrusader());
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());
        int power = elf.getEffectivePower();
        int toughness = elf.getEffectiveToughness();

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(crusader.isTapped()).isTrue();
        assertThat(elf.getEffectivePower()).isEqualTo(power + 2);
        assertThat(elf.getEffectiveToughness()).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Ability can target an opponent's Soldier")
    void boostsOpponentSoldier() {
        addCreatureReady(player1, new GrasslandCrusader());
        Permanent soldier = addCreatureReady(player2, new GlorySeeker());
        int power = soldier.getEffectivePower();
        int toughness = soldier.getEffectiveToughness();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.getEffectivePower()).isEqualTo(power + 2);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is neither an Elf nor a Soldier")
    void rejectsOtherCreature() {
        addCreatureReady(player1, new GrasslandCrusader());
        Permanent otherCreature = addCreatureReady(player1, new BarkhideMauler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new GrasslandCrusader());
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());
        int power = elf.getEffectivePower();
        int toughness = elf.getEffectiveToughness();

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();
        assertThat(elf.getEffectivePower()).isEqualTo(power + 2);
        assertThat(elf.getEffectiveToughness()).isEqualTo(toughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elf.getEffectivePower()).isEqualTo(power);
        assertThat(elf.getEffectiveToughness()).isEqualTo(toughness);
    }
}
