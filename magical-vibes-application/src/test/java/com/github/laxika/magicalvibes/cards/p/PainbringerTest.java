package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.FledglingImp;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Painbringer.class, Firebolt.class, DwarvenGrunt.class, FledglingImp.class})
class PainbringerTest extends BaseCardTest {

    @Test
    void exilesSelectedCardsAndUsesTheirCountForMinusXMinusX() {
        Permanent painbringer = setupPainbringer();
        Firebolt firebolt = new Firebolt();
        DwarvenGrunt dwarvenGrunt = new DwarvenGrunt();
        harness.setGraveyard(player1, List.of(firebolt, dwarvenGrunt));
        Permanent target = addCreatureReady(player2, new FledglingImp());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(firebolt.getId(), dwarvenGrunt.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(firebolt, dwarvenGrunt);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(painbringer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Fledgling Imp");
    }

    @Test
    void zeroExiledCardsLeavesTargetUnchanged() {
        Permanent painbringer = setupPainbringer();
        Permanent target = addCreatureReady(player2, new FledglingImp());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(painbringer.isTapped()).isTrue();
    }

    @Test
    void minusXMinusXWearsOffAtCleanup() {
        setupPainbringer();
        Firebolt firebolt = new Firebolt();
        harness.setGraveyard(player1, List.of(firebolt));
        Permanent target = addCreatureReady(player2, new FledglingImp());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firebolt.getId()));
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
    void onlyExilesCardsFromItsControllersGraveyard() {
        setupPainbringer();
        Firebolt firebolt = new Firebolt();
        harness.setGraveyard(player2, List.of(firebolt));
        Permanent target = addCreatureReady(player2, new FledglingImp());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firebolt);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void cannotTargetAPlayer() {
        setupPainbringer();
        Firebolt firebolt = new Firebolt();
        harness.setGraveyard(player1, List.of(firebolt));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firebolt);
    }

    private Permanent setupPainbringer() {
        return addCreatureReady(player1, new Painbringer());
    }
}
