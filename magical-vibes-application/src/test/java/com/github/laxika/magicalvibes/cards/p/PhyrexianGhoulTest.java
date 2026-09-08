package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives Phyrexian Ghoul +2/+2")
    void sacrificingCreatureBoostsPhyrexianGhoul() {
        Permanent ghoul = addReadyGhoul(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(4);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Phyrexian Ghoul can sacrifice itself")
    void canSacrificeItself() {
        addReadyGhoul(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Phyrexian Ghoul");
        harness.assertInGraveyard(player1, "Phyrexian Ghoul");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ghoul = addReadyGhoul(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(ghoul.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(2);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyGhoul(Player player) {
        Permanent ghoul = new Permanent(new PhyrexianGhoul());
        ghoul.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(ghoul);
        return ghoul;
    }
}
