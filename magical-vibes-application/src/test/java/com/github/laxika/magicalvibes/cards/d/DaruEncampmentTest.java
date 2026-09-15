package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaruEncampment.class, GlorySeeker.class, ElvishWarrior.class})
class DaruEncampmentTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new DaruEncampment());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a target Soldier +1/+1 until end of turn")
    void boostsTargetSoldier() {
        addEncampmentReady(player1);
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        int power = soldier.getEffectivePower();
        int toughness = soldier.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.getEffectivePower()).isEqualTo(power + 1);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(toughness + 1);
    }

    @Test
    @DisplayName("Ability can target an opponent's Soldier")
    void boostsOpponentSoldier() {
        addEncampmentReady(player1);
        Permanent soldier = addCreatureReady(player2, new GlorySeeker());
        int power = soldier.getEffectivePower();
        int toughness = soldier.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.getEffectivePower()).isEqualTo(power + 1);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(toughness + 1);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is not a Soldier")
    void rejectsNonSoldier() {
        addEncampmentReady(player1);
        Permanent nonSoldier = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonSoldier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability costs white mana and taps Daru Encampment")
    void abilityPaysWhiteManaAndTapsSource() {
        Permanent encampment = addEncampmentReady(player1);
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, soldier.getId());

        assertThat(encampment.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void rejectsNoncreaturePermanent() {
        addEncampmentReady(player1);
        Permanent target = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires white mana")
    void requiresWhiteMana() {
        Permanent encampment = addEncampmentReady(player1);
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(encampment.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addEncampmentReady(player1);
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        int power = soldier.getEffectivePower();
        int toughness = soldier.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, soldier.getId());
        harness.passBothPriorities();
        assertThat(soldier.getEffectivePower()).isEqualTo(power + 1);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(toughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(soldier.getEffectivePower()).isEqualTo(power);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(toughness);
    }

    private Permanent addEncampmentReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent encampment = harness.addToBattlefieldAndReturn(player, new DaruEncampment());
        encampment.setSummoningSick(false);
        return encampment;
    }
}
