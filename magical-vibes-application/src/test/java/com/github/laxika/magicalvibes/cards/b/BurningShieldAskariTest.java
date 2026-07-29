package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningShieldAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingPutsOnStack() {
        addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability grants first strike")
    void resolvingGrantsFirstStrike() {
        Permanent askari = addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent askari = addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough red mana")
    void cannotActivateWithoutMana() {
        addAskariReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability does not tap the creature and works while summoning sick")
    void activatingNeedsNoTapOrHaste() {
        Permanent askari = new Permanent(new BurningShieldAskari());
        gd.playerBattlefields.get(player1.getId()).add(askari);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(askari.isTapped()).isFalse();
    }

    private Permanent addAskariReady(Player player) {
        Permanent perm = new Permanent(new BurningShieldAskari());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
