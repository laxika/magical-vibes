package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.StringOfDisappearances;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousGiant.class, StringOfDisappearances.class})
class RavenousGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller during their upkeep")
    void dealsDamageDuringControllerUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new RavenousGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new RavenousGiant());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Each Giant damages only its own controller during that controller's upkeep")
    void eachGiantDamagesItsOwnController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new RavenousGiant());
        harness.addToBattlefield(player2, new RavenousGiant());

        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Two Giants each deal one damage during their controller's upkeep")
    void multipleGiantsTriggerSeparately() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new RavenousGiant());
        harness.addToBattlefield(player1, new RavenousGiant());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 10);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("The upkeep trigger still deals damage after the Giant returns to hand")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new RavenousGiant());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.setHand(player1, List.of(new StringOfDisappearances()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Ravenous Giant");
        harness.assertInHand(player1, "Ravenous Giant");
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 10);
    }
}
