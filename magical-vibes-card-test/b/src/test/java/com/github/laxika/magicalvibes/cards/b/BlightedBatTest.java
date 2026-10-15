package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BlightedBat.class})
class BlightedBatTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} grants haste until end of turn")
    void payingOneGrantsHaste() {
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bat.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bat.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bat.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("A newly entered bat can activate and attack after haste resolves")
    void newlyEnteredBatCanAttackAfterActivation() {
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new BlightedBat());
        bat.setSummoningSick(true);
        Permanent otherBat = harness.addToBattlefieldAndReturn(player1, new BlightedBat());
        otherBat.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(bat.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        harness.passBothPriorities();

        assertThat(otherBat.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bat.isAttacking()).isTrue();
        assertThat(otherBat.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Activation requires payment of one mana")
    void cannotActivateWithoutMana() {
        Permanent bat = addCreatureReady(player1, new BlightedBat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(bat.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("The haste ability can be activated while the bat is tapped")
    void canActivateWhileTapped() {
        Permanent bat = addCreatureReady(player1, new BlightedBat());
        bat.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bat.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(bat.isTapped()).isTrue();
    }
}
