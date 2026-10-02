package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SlimyDualleech;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurgulQuagNurturer.class, SlimyDualleech.class, GrizzlyBears.class})
class FurgulQuagNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Slimy Dualleech at the end of the turn when no Leeches are controlled")
    void conjuresSlimyDualleechWithoutLeeches() {
        harness.addToBattlefield(player1, new FurgulQuagNurturer());

        runToEndStep();

        assertThat(findPermanents(player1, "Slimy Dualleech")).hasSize(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on each Leech when one is already controlled")
    void growsControlledLeeches() {
        harness.addToBattlefield(player1, new FurgulQuagNurturer());
        Permanent firstLeech = harness.addToBattlefieldAndReturn(player1, new SlimyDualleech());
        Permanent secondLeech = harness.addToBattlefieldAndReturn(player1, new SlimyDualleech());

        runToEndStep();

        assertThat(gqs.getEffectivePower(gd, firstLeech)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondLeech)).isEqualTo(3);
        assertThat(findPermanents(player1, "Slimy Dualleech")).hasSize(2);
    }

    @Test
    @DisplayName("Tapping and sacrificing a creature adds green mana equal to its power")
    void sacrificesCreatureForItsPowerInGreenMana() {
        Permanent furgul = harness.addToBattlefieldAndReturn(player1, new FurgulQuagNurturer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(furgul), null, null);
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(furgul.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void runToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
