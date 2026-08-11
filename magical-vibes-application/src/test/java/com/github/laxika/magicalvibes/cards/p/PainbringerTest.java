package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PainbringerTest extends BaseCardTest {

    @Test
    void exilesSelectedCardsAndUsesTheirCountForMinusXMinusX() {
        Permanent painbringer = setupPainbringer();
        Shock shock = new Shock();
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(shock, elves));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock, elves);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(painbringer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void zeroExiledCardsLeavesTargetUnchanged() {
        Permanent painbringer = setupPainbringer();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(painbringer.isTapped()).isTrue();
    }

    @Test
    void minusXMinusXWearsOffAtCleanup() {
        Permanent painbringer = setupPainbringer();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void cannotTargetAPlayer() {
        setupPainbringer();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
    }

    private Permanent setupPainbringer() {
        return addCreatureReady(player1, new Painbringer());
    }
}
