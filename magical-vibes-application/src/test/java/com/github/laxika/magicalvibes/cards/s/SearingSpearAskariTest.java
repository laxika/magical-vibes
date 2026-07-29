package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearingSpearAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants menace until end of turn")
    void resolvingAbilityGrantsMenace() {
        Permanent askari = addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, askari, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace wears off at end of turn")
    void menaceWearsOff() {
        Permanent askari = addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, askari, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Searing Spear Askari")
    void activatingDoesNotTap() {
        Permanent askari = addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(askari.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires red mana")
    void requiresRedMana() {
        addAskariReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addAskariReady(Player player) {
        Permanent perm = new Permanent(new SearingSpearAskari());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
