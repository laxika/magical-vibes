package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrMoonvessel.class, EchoingRuin.class})
class MyrMoonvesselTest extends BaseCardTest {

    @Test
    @DisplayName("When Myr Moonvessel dies, its controller adds a colorless mana")
    void diesAddsColorlessMana() {
        Permanent moonvessel = harness.addToBattlefieldAndReturn(player1, new MyrMoonvessel());
        harness.setHand(player2, List.of(new EchoingRuin()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, moonvessel.getId());

        harness.assertInGraveyard(player1, "Myr Moonvessel");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Simultaneous Moonvessel deaths each give mana to their own controller")
    void simultaneousDeathsAwardManaToEachController() {
        Permanent moonvessel = harness.addToBattlefieldAndReturn(player1, new MyrMoonvessel());
        harness.addToBattlefield(player2, new MyrMoonvessel());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, moonvessel.getId());

        harness.assertInGraveyard(player1, "Myr Moonvessel");
        harness.assertInGraveyard(player2, "Myr Moonvessel");
        harness.assertNotOnBattlefield(player1, "Myr Moonvessel");
        harness.assertNotOnBattlefield(player2, "Myr Moonvessel");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
