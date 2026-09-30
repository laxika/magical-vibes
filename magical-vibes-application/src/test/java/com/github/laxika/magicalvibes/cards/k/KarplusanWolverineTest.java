package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarplusanWolverine.class, BorealCentaur.class})
class KarplusanWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("When blocked, accepting deals 1 damage to a target creature")
    void acceptingDealsDamageToTargetCreature() {
        List<Permanent> blockers = declareBlockedCombat(1);
        Permanent blocker = blockers.getFirst();

        chooseTargetAndAnswer(blocker.getId(), true);

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("When blocked, accepting deals 1 damage to a target player")
    void acceptingDealsDamageToTargetPlayer() {
        declareBlockedCombat(1);
        harness.setLife(player2, 20);

        chooseTargetAndAnswer(player2.getId(), true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        List<Permanent> blockers = declareBlockedCombat(1);
        Permanent blocker = blockers.getFirst();

        chooseTargetAndAnswer(blocker.getId(), false);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates only one may ability")
    void multipleBlockersCreateOneAbility() {
        declareBlockedCombat(2);

        chooseTargetAndAnswer(player2.getId(), true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unblocked Karplusan Wolverine does not trigger")
    void unblockedDoesNotTrigger() {
        addCreatureReady(player1, new KarplusanWolverine());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> declareBlockedCombat(int blockerCount) {
        addCreatureReady(player1, new KarplusanWolverine());

        List<Permanent> blockers = new ArrayList<>();
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int blockerIndex = 0; blockerIndex < blockerCount; blockerIndex++) {
            blockers.add(addCreatureReady(player2, new BorealCentaur()));
            assignments.add(new BlockerAssignment(blockerIndex, 0));
        }

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        return blockers;
    }

    private void chooseTargetAndAnswer(UUID targetId, boolean accept) {
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(targetId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
