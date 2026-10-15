package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({OnyxMage.class, RuneclawBear.class})
class OnyxMageTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants deathtouch to target creature you control")
    void grantsDeathtouchToOwnCreature() {
        addCreatureReady(player1, new OnyxMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Granted deathtouch wears off at end of turn")
    void deathtouchWearsOff() {
        addCreatureReady(player1, new OnyxMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new OnyxMage());
        Permanent oppBears = addCreatureReady(player2, new RuneclawBear());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, oppBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability requires {1}{B}")
    void requiresMana() {
        addCreatureReady(player1, new OnyxMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Mage can target itself, granting deathtouch only on resolution")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new OnyxMage());
        mage.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, mage.getId());

        assertThat(gqs.hasKeyword(gd, mage, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.DEATHTOUCH)).isTrue();
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mage can grant deathtouch to multiple creatures through repeated activations")
    void canActivateRepeatedly() {
        Permanent mage = addCreatureReady(player1, new OnyxMage());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, mage.getId());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(mage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Generic mana cannot replace the required black mana")
    void requiresBlackMana() {
        Permanent mage = addCreatureReady(player1, new OnyxMage());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, mage, Keyword.DEATHTOUCH)).isFalse();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
