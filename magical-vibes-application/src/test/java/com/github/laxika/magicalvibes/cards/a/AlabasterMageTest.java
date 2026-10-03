package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({AlabasterMage.class, RuneclawBear.class})
class AlabasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants lifelink to target creature you control")
    void grantsLifelinkToOwnCreature() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOff() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent oppBears = addCreatureReady(player2, new RuneclawBear());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, oppBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires {1}{W}")
    void requiresMana() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Mage can grant itself lifelink")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new AlabasterMage());
        mage.setSummoningSick(true);
        mage.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, mage.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repeated grants of lifelink gain life only once for combat damage")
    void repeatedActivationsDoNotMultiplyLifeGain() {
        addCreatureReady(player1, new AlabasterMage());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        for (int activation = 0; activation < 2; activation++) {
            addAbilityMana();
            harness.activateAbility(player1, 0, null, bear.getId());
            harness.passBothPriorities();
        }

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability still resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent mage = addCreatureReady(player1, new AlabasterMage());
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();
        harness.activateAbility(player1, 0, null, bear.getId());

        gd.playerBattlefields.get(player1.getId()).remove(mage);
        harness.setGraveyard(player1, List.of(mage.getCard()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
