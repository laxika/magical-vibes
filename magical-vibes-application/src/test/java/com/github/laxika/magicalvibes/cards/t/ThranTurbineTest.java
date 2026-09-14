package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChimericStaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranTurbine.class, ChimericStaff.class})
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
        harness.setHand(player1, List.of(new ThranTurbine()));
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the upkeep trigger adds no mana")
    void declinedTriggerAddsNoMana() {
        harness.addToBattlefield(player1, new ThranTurbine());

        stopAtUpkeep();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Ability-only mana cannot cast a spell but can pay an activated ability")
    void manaCannotCastSpellButPaysAbility() {
        harness.addToBattlefield(player1, new ThranTurbine());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new ChimericStaff());

        stopAtUpkeep();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, staff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, staff)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger fires only during its controller's upkeep")
    void triggerOnlyFiresDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new ThranTurbine());

        stopAtUpkeep();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }
}
