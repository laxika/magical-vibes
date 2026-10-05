package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoorlandInquisitor.class})
class MoorlandInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants first strike")
    void resolvingGrantsFirstStrike() {
        Permanent inquisitor = addInquisitorReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent inquisitor = addInquisitorReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addInquisitorReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability needs no tap and works while summoning sick")
    void activatingNeedsNoTapOrHaste() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(inquisitor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability requires white mana")
    void cannotActivateWithoutWhiteMana() {
        addInquisitorReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped creature can activate using generic and white mana")
    void tappedCreatureCanActivate() {
        Permanent inquisitor = addInquisitorReady(player1);
        inquisitor.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(inquisitor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the activating creature gains first strike")
    void grantsFirstStrikeOnlyToSource() {
        Permanent inquisitor = addInquisitorReady(player1);
        Permanent ally = addInquisitorReady(player1);
        Permanent opponent = addInquisitorReady(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addInquisitorReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MoorlandInquisitor());
        perm.setSummoningSick(false);
        return perm;
    }
}
