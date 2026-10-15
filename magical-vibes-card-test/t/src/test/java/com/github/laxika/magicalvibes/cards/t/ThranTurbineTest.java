package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlbinoTroll;
import com.github.laxika.magicalvibes.cards.c.ChimericStaff;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranTurbine.class, ChimericStaff.class, AlbinoTroll.class, Disenchant.class})
class ThranTurbineTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds two ability-only colorless mana when accepted")
    void acceptedTriggerAddsAbilityOnlyMana() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            Permanent turbine = harness.addToBattlefieldAndReturn(player1, new ThranTurbine());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);

            harness.setHand(player1, List.of(new Disenchant()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            assertThatThrownBy(() -> harness.castInstant(player1, 0, turbine.getId()))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Declining the upkeep trigger adds no mana")
    void declinedTriggerAddsNoMana() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Ability-only mana cannot cast a spell but can pay an activated ability")
    void manaCannotCastSpellButPaysAbility() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());
            Permanent staff = harness.addToBattlefieldAndReturn(player1, new ChimericStaff());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.activateAbility(player1, 1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, staff)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, staff)).isEqualTo(2);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Upkeep trigger fires only during its controller's upkeep")
    void triggerOnlyFiresDuringControllerUpkeep() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());

            advanceToUpkeep(player2);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Unused Turbine mana empties before the draw step")
    void unusedManaEmptiesAtEndOfUpkeep() {
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);
        });
        harness.passUntil(player1, TurnStep.DRAW);

        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Turbine mana can pay the generic portion of an echo cost")
    void manaCanPayEchoCost() {
        harness.setHand(player1, List.of(new AlbinoTroll()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);

            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Albino Troll");
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(1);
        });
    }
}
