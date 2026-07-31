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

class StonehornChanterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants both vigilance and lifelink")
    void resolvingGrantsVigilanceAndLifelink() {
        Permanent chanter = addChanterReady(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent chanter = addChanterReady(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addChanterReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability does not tap Stonehorn Chanter")
    void activatingDoesNotTap() {
        Permanent chanter = addChanterReady(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(chanter.isTapped()).isFalse();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }

    private Permanent addChanterReady(Player player) {
        Permanent perm = new Permanent(new StonehornChanter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
