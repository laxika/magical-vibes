package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AureliasFuryTest extends BaseCardTest {

    /** Index 0 is a noncreature spell (Giant Growth), index 1 a creature spell (Grizzly Bears). */
    private List<Integer> player2Playable() {
        // Giant Growth needs a legal creature target, or it would be unplayable for the wrong reason.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());
    }

    private void castFuryForX(int xValue, Map<UUID, Integer> assignments) {
        harness.setHand(player1, List.of(new AureliasFury()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstantForX(player1, 0, xValue, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Divides X damage among a creature and a player, tapping the damaged creature")
    void dividesDamageAndTapsDamagedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFuryForX(2, Map.of(bears.getId(), 1, player2.getId(), 1));

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A player dealt damage this way can't cast noncreature spells, but creature spells are fine")
    void damagedPlayerCantCastNoncreatureSpells() {
        castFuryForX(1, Map.of(player2.getId(), 1));

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0);
        assertThat(playable).contains(1);
    }

    @Test
    @DisplayName("A player who wasn't dealt damage isn't locked out of noncreature spells")
    void undamagedPlayerCanStillCastNoncreatureSpells() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // All the damage goes to the creature, so no player is dealt damage this way.
        castFuryForX(1, Map.of(bears.getId(), 1));

        assertThat(player2Playable()).contains(0);
    }

    @Test
    @DisplayName("The noncreature-spell lock wears off at end of turn")
    void lockWearsOffAtEndOfTurn() {
        castFuryForX(1, Map.of(player2.getId(), 1));
        assertThat(player2Playable()).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(player2Playable()).contains(0);
    }
}
