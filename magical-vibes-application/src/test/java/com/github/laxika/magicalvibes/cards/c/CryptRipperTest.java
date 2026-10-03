package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptRipper.class})
class CryptRipperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent ripper = addReadyCryptRipper(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(1);
        assertThat(ripper.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent ripper = addReadyCryptRipper(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(2);
        assertThat(ripper.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent ripper = addReadyCryptRipper(player1);
        ripper.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyCryptRipper(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent ripper = addReadyCryptRipper(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ripper.getPowerModifier()).isEqualTo(0);
        assertThat(ripper.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can attack on the turn it is cast due to haste")
    void canAttackOnTheTurnItIsCast() {
        CryptRipper card = new CryptRipper();
        harness.castFromHand(player1, card, "{2}{B}{B}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Crypt Ripper").isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Pending activations only boost their source when they resolve")
    void pendingActivationsBoostOnlyTheirSource() {
        Permanent ripper = harness.addToBattlefieldAndReturn(player1, new CryptRipper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CryptRipper());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(ripper.getPowerModifier()).isZero();
        assertThat(ripper.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(ripper.getPowerModifier()).isEqualTo(1);
        assertThat(ripper.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(ripper.getPowerModifier()).isEqualTo(2);
        assertThat(ripper.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    private Permanent addReadyCryptRipper(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CryptRipper());
        perm.setSummoningSick(false);
        return perm;
    }
}
