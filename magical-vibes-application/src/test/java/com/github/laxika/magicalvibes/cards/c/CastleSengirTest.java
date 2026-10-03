package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CastleSengir.class)
class CastleSengirTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for {C} adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new CastleSengir());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T} spends one generic mana and adds {B}")
    void tapForBlack() {
        harness.addToBattlefield(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{2}, {T} adds the chosen one of {U} or {R}")
    void tapForBlueOrRed() {
        harness.addToBattlefield(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{2}, {T} can add blue mana")
    void tapForBlue() {
        harness.addToBattlefield(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana abilities require Castle Sengir to be untapped")
    void manaAbilitiesRequireUntappedCastle() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleSengir());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(castle.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Costed abilities cannot be activated without mana to pay")
    void costedAbilitiesRequireMana() {
        harness.addToBattlefield(player1, new CastleSengir());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The generic cost for black mana can be paid with colored mana")
    void blackAbilityAcceptsColoredMana() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blue or red ability accepts colored mana and taps as a cost")
    void blueOrRedAbilityAcceptsColoredManaAndTapsBeforeChoice() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("One mana cannot pay the blue or red ability and a failed activation preserves resources")
    void blueOrRedAbilityRequiresTwoMana() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleSengir());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(castle.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
