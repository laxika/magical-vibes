package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitiatesOfTheEbonHandTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability adds {B}")
    void activatingAddsBlackMana() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Initiates of the Ebon Hand");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four or more times")
    void sacrificedWhenActivatedFourOrMoreTimes() {
        addReadyInitiates(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Initiates of the Ebon Hand");
        harness.assertInGraveyard(player1, "Initiates of the Ebon Hand");
    }

    private Permanent addReadyInitiates(Player player) {
        InitiatesOfTheEbonHand card = new InitiatesOfTheEbonHand();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
