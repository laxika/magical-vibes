package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GuardianShieldBearer;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CraterElemental.class, SummitProwler.class, GuardianShieldBearer.class})
class CraterElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability deals 4 damage to a target creature")
    void sacrificeAbilityDealsDamage() {
        addCreatureReady(player1, new CraterElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Crater Elemental");
        harness.assertInGraveyard(player1, "Crater Elemental");
        harness.assertOnBattlefield(player2, "Summit Prowler");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Summit Prowler");
    }

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void formidableRequiresTotalPowerEight() {
        harness.addToBattlefield(player1, new CraterElemental());
        addShieldBearers(3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable ability sets base power to 8 until end of turn")
    void formidableSetsBasePowerUntilEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CraterElemental());
        addShieldBearers(4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrifice ability deals exactly four damage to a surviving creature")
    void sacrificeAbilityMarksFourDamage() {
        addCreatureReady(player1, new CraterElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CraterElemental());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Crater Elemental");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated while tapped")
    void sacrificeAbilityRequiresUntappedSource() {
        Permanent source = addCreatureReady(player1, new CraterElemental());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Crater Elemental");
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated with summoning sickness")
    void sacrificeAbilityRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new CraterElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Crater Elemental");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a player")
    void sacrificeAbilityRejectsPlayerTarget() {
        addCreatureReady(player1, new CraterElemental());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Crater Elemental");
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to formidable")
    void formidableIgnoresOpponentPower() {
        harness.addToBattlefield(player1, new CraterElemental());
        addShieldBearers(3);
        harness.addToBattlefield(player2, new SummitProwler());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable resolves even when total power falls below eight in response")
    void formidableDoesNotRecheckPowerAtResolution() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CraterElemental());
        addShieldBearers(4);
        Permanent target = findPermanent(player1, "Guardian Shield-Bearer");
        addCreatureReady(player2, new CraterElemental());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Guardian Shield-Bearer")).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elemental)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(8);
    }

    @Test
    @DisplayName("Formidable counts the source's modified power and preserves its counters")
    void formidableUsesEffectivePowerAndPreservesCounters() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CraterElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        elemental.tap();
        addShieldBearers(3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(8);
        assertThat(elemental.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(8);
    }

    private void addShieldBearers(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new GuardianShieldBearer());
        }
    }
}
