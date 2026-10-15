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

@CardUsed({StonefareCrocodile.class})
class StonefareCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{B} grants lifelink until end of turn")
    void activationGrantsLifelink() {
        Permanent crocodile = addCreatureReady(player1, new StonefareCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crocodile.getGrantedKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent crocodile = addCreatureReady(player1, new StonefareCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crocodile.getGrantedKeywords()).contains(Keyword.LIFELINK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crocodile.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new StonefareCrocodile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted lifelink gains life from combat damage")
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player1, new StonefareCrocodile());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Multiple activations do not multiply lifelink life gain")
    void repeatedActivationDoesNotMultiplyLifeGain() {
        addCreatureReady(player1, new StonefareCrocodile());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A tapped, summoning-sick crocodile can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent crocodile = addCreatureReady(player1, new StonefareCrocodile());
        crocodile.setSummoningSick(true);
        crocodile.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(crocodile.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
        harness.passBothPriorities();

        assertThat(crocodile.getGrantedKeywords()).contains(Keyword.LIFELINK);
        assertThat(crocodile.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activation requires black mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new StonefareCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
