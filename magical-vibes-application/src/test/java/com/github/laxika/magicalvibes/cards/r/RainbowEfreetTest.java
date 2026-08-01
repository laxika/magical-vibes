package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RainbowEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("The {U}{U} ability phases Rainbow Efreet out")
    void phasesOut() {
        Permanent efreet = addReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(efreet);
    }

    @Test
    @DisplayName("Rainbow Efreet phases back in during its controller's next untap step")
    void phasesBackIn() {
        Permanent efreet = addReady(player1);
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

    private Permanent addReady(Player player) {
        Permanent perm = new Permanent(new RainbowEfreet());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
