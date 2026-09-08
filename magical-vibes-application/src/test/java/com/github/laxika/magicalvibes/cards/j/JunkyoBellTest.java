package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JunkyoBellTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting boosts the target by the number of creatures controlled")
    void acceptBoostsByCreatureCount() {
        addBell(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());

        acceptTargeting(bears);

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boosted creature is sacrificed at the beginning of the next end step")
    void boostedCreatureIsSacrificedAtEndStep() {
        addBell(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        acceptTargeting(bears);
        passToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining leaves the creature unboosted and alive")
    void declineDoesNothing() {
        addBell(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getPowerModifier()).isZero();

        passToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("A creature an opponent controls is not a legal target")
    void opponentCreatureIsNotATarget() {
        addBell(player1);
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new LlanowarElves());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mine.getId())
                .doesNotContain(theirs.getId());

        harness.handlePermanentChosen(player1, mine.getId());
    }

    private void addBell(Player player) {
        addCreatureReady(player, new JunkyoBell());
    }

    private void acceptTargeting(Permanent target) {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void passToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
