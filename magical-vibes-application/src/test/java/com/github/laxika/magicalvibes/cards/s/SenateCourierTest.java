package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SenateCourier.class})
class SenateCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants vigilance until end of turn")
    void resolvingAbilityGrantsVigilance() {
        Permanent courier = addCreatureReady(player1, new SenateCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent courier = addCreatureReady(player1, new SenateCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one white and one generic mana")
    void abilityRequiresOneWhiteAndOneGenericMana() {
        addCreatureReady(player1, new SenateCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Generic mana cannot replace the white mana requirement")
    void abilityRequiresWhiteMana() {
        addCreatureReady(player1, new SenateCourier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Courier can activate without untapping")
    void tappedSummoningSickCourierCanActivate() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        courier.setSummoningSick(true);
        courier.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isTrue();
        assertThat(courier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the Courier whose ability resolves gains vigilance")
    void grantsVigilanceOnlyToSource() {
        Permanent courier = addCreatureReady(player1, new SenateCourier());
        Permanent otherCourier = addCreatureReady(player1, new SenateCourier());
        Permanent opposingCourier = addCreatureReady(player2, new SenateCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, courier, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCourier, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCourier, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A Courier with granted vigilance attacks without tapping")
    void grantedVigilancePreventsTappingToAttack() {
        Permanent courier = addCreatureReady(player1, new SenateCourier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(courier.isAttacking()).isTrue();
        assertThat(courier.isTapped()).isFalse();
    }
}
