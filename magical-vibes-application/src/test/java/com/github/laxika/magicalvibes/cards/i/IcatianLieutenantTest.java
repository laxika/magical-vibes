package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BrassclawOrcs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianLieutenant.class, IcatianJavelineers.class, BrassclawOrcs.class})
class IcatianLieutenantTest extends BaseCardTest {

    private void addLieutenant() {
        addCreatureReady(player1, new IcatianLieutenant());
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    @Test
    @DisplayName("Ability gives target Soldier +1/+0 until end of turn")
    void boostsTargetSoldier() {
        addLieutenant();
        harness.addToBattlefield(player1, new IcatianJavelineers());

        UUID targetId = harness.getPermanentId(player1, "Icatian Javelineers");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Icatian Javelineers");
        assertThat(soldier.getEffectivePower()).isEqualTo(2);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can target a Soldier creature controlled by an opponent")
    void boostsOpponentsSoldier() {
        addLieutenant();
        harness.addToBattlefield(player2, new IcatianJavelineers());

        UUID targetId = harness.getPermanentId(player2, "Icatian Javelineers");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player2, "Icatian Javelineers");
        assertThat(soldier.getEffectivePower()).isEqualTo(2);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addLieutenant();
        harness.addToBattlefield(player1, new IcatianJavelineers());

        UUID targetId = harness.getPermanentId(player1, "Icatian Javelineers");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Icatian Javelineers");
        assertThat(soldier.getEffectivePower()).isEqualTo(1);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a non-Soldier creature")
    void rejectsNonSoldierTarget() {
        addLieutenant();
        harness.addToBattlefield(player1, new BrassclawOrcs());

        UUID targetId = harness.getPermanentId(player1, "Brassclaw Orcs");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires one generic and one white mana")
    void requiresGenericAndWhiteMana() {
        addCreatureReady(player1, new IcatianLieutenant());
        harness.addToBattlefield(player1, new IcatianJavelineers());
        UUID targetId = harness.getPermanentId(player1, "Icatian Javelineers");

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires white mana in addition to generic mana")
    void requiresWhiteMana() {
        addCreatureReady(player1, new IcatianLieutenant());
        harness.addToBattlefield(player1, new IcatianJavelineers());
        UUID targetId = harness.getPermanentId(player1, "Icatian Javelineers");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
