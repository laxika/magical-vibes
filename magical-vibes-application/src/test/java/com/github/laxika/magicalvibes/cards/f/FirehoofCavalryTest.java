package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FirehoofCavalry.class})
class FirehoofCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 and trample until end of turn")
    void resolvingAbilityBoostsSelfAndGrantsTrample() {
        Permanent cavalry = addReadyCavalry();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(2);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(0);
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent cavalry = addReadyCavalry();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(0);
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack the power boost and affect only the source")
    void repeatedActivationsBoostOnlySource() {
        Permanent cavalry = addReadyCavalry();
        Permanent other = addCreatureReady(player1, new FirehoofCavalry());
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(4);
        assertThat(cavalry.getToughnessModifier()).isZero();
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new FirehoofCavalry());
        cavalry.setSummoningSick(true);
        cavalry.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isEqualTo(2);
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(cavalry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Four mana without red cannot pay for the ability")
    void activationRequiresRedMana() {
        Permanent cavalry = addReadyCavalry();
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(cavalry.getPowerModifier()).isZero();
        assertThat(cavalry.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCavalry() {
        return addCreatureReady(player1, new FirehoofCavalry());
    }
}
