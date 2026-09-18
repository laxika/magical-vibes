package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZukoFirebendingMaster.class, Shock.class})
class ZukoFirebendingMasterTest extends BaseCardTest {

    @Test
    void firebendingAddsManaEqualToExperienceCountersUntilEndOfCombat() {
        addReadyZuko();
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void castingASpellDuringCombatGivesAnExperienceCounter() {
        addReadyZuko();
        castShockDuringCombat();

        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void castingASpellOutsideCombatDoesNotGiveAnExperienceCounter() {
        addReadyZuko();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    private Permanent addReadyZuko() {
        return addCreatureReady(player1, new ZukoFirebendingMaster());
    }

    private void castShockDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
