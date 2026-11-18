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
        int power = gqs.getEffectivePower(gd, elf);
        int toughness = gqs.getEffectiveToughness(gd, elf);

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(crusader.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Ability can target an opponent's Soldier")
    void boostsOpponentSoldier() {
        addCreatureReady(player1, new GrasslandCrusader());
        Permanent soldier = addCreatureReady(player2, new GlorySeeker());
        int power = gqs.getEffectivePower(gd, soldier);
        int toughness = gqs.getEffectiveToughness(gd, soldier);

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(toughness + 2);
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
        int power = gqs.getEffectivePower(gd, elf);
        int toughness = gqs.getEffectiveToughness(gd, elf);

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(toughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Crusader can target itself as a Soldier")
    void boostsItself() {
        Permanent crusader = addCreatureReady(player1, new GrasslandCrusader());
        int power = gqs.getEffectivePower(gd, crusader);
        int toughness = gqs.getEffectiveToughness(gd, crusader);

        harness.activateAbility(player1, 0, null, crusader.getId());
        harness.passBothPriorities();

        assertThat(crusader.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent crusader = addCreatureReady(player1, new GrasslandCrusader());
        crusader.setSummoningSick(true);
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(crusader.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Crusader cannot activate again")
    void cannotActivateAgainWithoutUntapping() {
        addCreatureReady(player1, new GrasslandCrusader());
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());
        int power = gqs.getEffectivePower(gd, elf);
        int toughness = gqs.getEffectiveToughness(gd, elf);

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Ability resolves even if Crusader leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent crusader = addCreatureReady(player1, new GrasslandCrusader());
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());
        int power = gqs.getEffectivePower(gd, elf);
        int toughness = gqs.getEffectiveToughness(gd, elf);

        harness.activateAbility(player1, 0, null, elf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(crusader);
        gd.playerGraveyards.get(player1.getId()).add(crusader.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(toughness + 2);
    }

    @Test
    @DisplayName("Ability does not boost another creature when its target leaves")
    void doesNotRetargetWhenTargetLeaves() {
        Permanent crusader = addCreatureReady(player1, new GrasslandCrusader());
        Permanent elf = addCreatureReady(player1, new ElvishPioneer());
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        int power = gqs.getEffectivePower(gd, soldier);
        int toughness = gqs.getEffectiveToughness(gd, soldier);

        harness.activateAbility(player1, 0, null, elf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elf);
        gd.playerGraveyards.get(player1.getId()).add(elf.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(crusader.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(toughness);
    }
}
