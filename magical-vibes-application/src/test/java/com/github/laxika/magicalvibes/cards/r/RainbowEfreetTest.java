package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RainbowEfreet.class)
class RainbowEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("The {U}{U} ability phases Rainbow Efreet out")
    void phasesOut() {
        Permanent efreet = addCreatureReady(player1, new RainbowEfreet());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);
    }

    @Test
    @DisplayName("The phase-out ability requires two blue mana")
    void requiresTwoBlueMana() {
        Permanent efreet = addCreatureReady(player1, new RainbowEfreet());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), java.util.List.of()))
                .doesNotContain(efreet);
    }

    @Test
    @DisplayName("The phase-out ability can be activated while Rainbow Efreet is tapped")
    void canActivateWhileTapped() {
        Permanent efreet = addCreatureReady(player1, new RainbowEfreet());
        efreet.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(efreet.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);
    }

    @Test
    @DisplayName("Phasing out removes Rainbow Efreet from combat")
    void phasesOutRemovesItFromCombat() {
        Permanent efreet = addCreatureReady(player1, new RainbowEfreet());
        efreet.setAttacking(true);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(efreet.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Rainbow Efreet phases back in during its controller's next untap step")
    void phasesBackIn() {
        Permanent efreet = addCreatureReady(player1, new RainbowEfreet());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);

        advanceTurn(); // player2's turn
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);

        advanceTurn(); // player1's untap — phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
