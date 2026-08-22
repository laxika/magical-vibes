package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pyric Salamander")
class PyricSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +1/+0 until end of turn")
    void activationBoostsPower() {
        Permanent salamander = new Permanent(new PyricSalamander());
        salamander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(salamander);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, salamander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, salamander)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability sacrifices it at the beginning of the next end step")
    void activationSacrificesAtNextEndStep() {
        Permanent salamander = new Permanent(new PyricSalamander());
        salamander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(salamander);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pyric Salamander");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pyric Salamander");
        harness.assertInGraveyard(player1, "Pyric Salamander");
    }

    @Test
    @DisplayName("Without activating the ability it stays on the battlefield")
    void survivesWithoutActivation() {
        Permanent salamander = new Permanent(new PyricSalamander());
        salamander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(salamander);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pyric Salamander");
    }
}
