package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DauthiEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shadow to target creature you control")
    void grantsShadowToOwnCreature() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Ability can grant shadow to a creature an opponent controls")
    void grantsShadowToOpponentCreature() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, oppBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, oppBears, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOff() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Ability requires {B}{B}")
    void requiresMana() {
        harness.addToBattlefield(player1, new DauthiEmbrace());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
