package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed(ObscuraInitiate.class)
class ObscuraInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants lifelink when paid with white mana")
    void gainsLifelinkWithWhiteMana() {
        Permanent initiate = addCreatureReady(player1, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Ability grants lifelink when paid with black mana")
    void gainsLifelinkWithBlackMana() {
        Permanent initiate = addCreatureReady(player1, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOff() {
        Permanent initiate = addCreatureReady(player1, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ability requires one generic mana and one white or black mana")
    void requiresGenericAndHybridMana() {
        addCreatureReady(player1, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Blue mana cannot pay the white or black hybrid symbol")
    void cannotPayHybridWithBlueMana() {
        addCreatureReady(player1, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick initiate can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new ObscuraInitiate());
        initiate.setSummoningSick(true);
        initiate.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lifelink is granted on resolution only to the activating initiate")
    void grantsLifelinkOnlyToSourceOnResolution() {
        Permanent initiate = addCreatureReady(player1, new ObscuraInitiate());
        Permanent other = addCreatureReady(player1, new ObscuraInitiate());
        Permanent opponent = addCreatureReady(player2, new ObscuraInitiate());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Repeated lifelink activations do not multiply combat life gain")
    void repeatedActivationsGainLifeOnlyOncePerDamage() {
        addCreatureReady(player1, new ObscuraInitiate());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
