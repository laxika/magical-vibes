package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThranTurbineTest extends BaseCardTest {

    private void stopAtUpkeep() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.UPKEEP));
    }

    @Test
    @DisplayName("Upkeep trigger adds two ability-only colorless mana when accepted")
    void acceptedTriggerAddsAbilityOnlyMana() {
        harness.addToBattlefield(player1, new ThranTurbine());

        stopAtUpkeep();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HowlingMine()));
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the upkeep trigger adds no mana")
    void declinedTriggerAddsNoMana() {
        harness.addToBattlefield(player1, new ThranTurbine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Ability-only mana cannot cast a spell but can pay an activated ability")
    void manaCannotCastSpellButPaysAbility() {
        harness.addToBattlefield(player1, new ThranTurbine());
        harness.addToBattlefield(player1, new FountainOfYouth());

        stopAtUpkeep();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }
}
