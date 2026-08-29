package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SireOfSevenDeathsTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller declines to pay 7 life")
    void wardCountersSpellWhenPaymentIsDeclined() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sire.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Sire of Seven Deaths");
    }

    @Test
    @DisplayName("Paying 7 life allows the targeting spell to resolve")
    void payingWardLifeCostAllowsSpellToResolve() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfSevenDeaths());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MightOfOaks()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, sire.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(gqs.getEffectivePower(gd, sire)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, sire)).isEqualTo(14);
    }
}
