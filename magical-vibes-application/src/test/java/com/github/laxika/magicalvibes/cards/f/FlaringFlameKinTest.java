package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElementalResonance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlaringFlameKin.class, ElementalResonance.class})
class FlaringFlameKinTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Aura, Flaring Flame-Kin is a 2/2 without trample or the firebreathing ability")
    void withoutAura() {
        Permanent kin = addKin();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("While enchanted, Flaring Flame-Kin gets +2/+2, trample, and its firebreathing ability")
    void whileEnchanted() {
        Permanent kin = addKin();
        addAura(kin);

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The firebreathing boost lasts only until end of turn")
    void firebreathingBoostExpiresAtEndOfTurn() {
        Permanent kin = addKin();
        addAura(kin);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flaring Flame-Kin loses the conditional abilities when the Aura leaves")
    void losesEffectsWhenAuraLeaves() {
        Permanent kin = addKin();
        Permanent aura = addAura(kin);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("An opponent-controlled Aura enables all conditional benefits")
    void opponentControlledAuraEnablesBenefits() {
        Permanent kin = addKin();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ElementalResonance());
        aura.setAttachedTo(kin.getId());

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple Auras do not multiply the bonus, and one remaining Aura keeps it enabled")
    void multipleAurasDoNotMultiplyBonus() {
        Permanent kin = addKin();
        Permanent firstAura = addAura(kin);
        addAura(kin);

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isTrue();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(5);
    }

    @Test
    @DisplayName("An activated firebreathing ability still resolves after the last Aura leaves")
    void activatedAbilityResolvesAfterAuraLeaves() {
        Permanent kin = addKin();
        Permanent aura = addAura(kin);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kin, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addKin() {
        return addCreatureReady(player1, new FlaringFlameKin());
    }

    private Permanent addAura(Permanent kin) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElementalResonance());
        aura.setAttachedTo(kin.getId());
        return aura;
    }
}
