package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WizardsSchool.class)
class WizardsSchoolTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new WizardsSchool());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T} spends one generic mana and adds {U}")
    void tapForBlue() {
        harness.addToBattlefield(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{2}, {T} adds the chosen one of {W} or {B}")
    void tapForWhiteOrBlack() {
        harness.addToBattlefield(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{2}, {T} can add white when white is chosen")
    void tapForWhite() {
        harness.addToBattlefield(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana abilities require Wizards' School to be untapped")
    void manaAbilitiesRequireUntappedSchool() {
        var school = harness.addToBattlefieldAndReturn(player1, new WizardsSchool());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(school.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Costed abilities cannot be activated without mana to pay")
    void costedAbilitiesRequireMana() {
        harness.addToBattlefield(player1, new WizardsSchool());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colored mana can pay the generic cost of the blue ability")
    void blueAbilityAcceptsColoredMana() {
        var school = harness.addToBattlefieldAndReturn(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(school.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two colored mana can pay for one chosen white mana")
    void whiteAbilitySpendsTwoColoredMana() {
        var school = harness.addToBattlefieldAndReturn(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(school.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient payment leaves the school untapped and mana unspent")
    void insufficientPaymentDoesNotSpendManaOrTapSchool() {
        var school = harness.addToBattlefieldAndReturn(player1, new WizardsSchool());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(school.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(school.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
