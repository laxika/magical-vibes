package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoseCutthroatRaider.class})
class RoseCutthroatRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates one Junk for each opponent attacked")
    void raidCreatesJunkForEachOpponentAttacked() {
        addReadyRose();

        declareAttackers(java.util.List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Junk adds red mana")
    void sacrificingJunkAddsRedMana() {
        addReadyRose();

        declareAttackers(java.util.List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent junk = findPermanent(player1, "Junk");
        int junkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(junk);
        harness.activateAbility(player1, junkIndex, null, null);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raid creates no Junk when you did not attack")
    void raidCreatesNoJunkWithoutAttacking() {
        addReadyRose();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Junk")).isEmpty();
    }

    private Permanent addReadyRose() {
        Permanent rose = harness.addToBattlefieldAndReturn(player1, new RoseCutthroatRaider());
        rose.setSummoningSick(false);
        return rose;
    }
}
