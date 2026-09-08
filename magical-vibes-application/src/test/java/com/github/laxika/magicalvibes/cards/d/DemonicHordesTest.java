package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicHordes.class, Island.class, GrizzlyBears.class})
class DemonicHordesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land")
    void destroysTargetLand() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Can destroy a land it controls")
    void destroysTargetLandItControls() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCreatureReady(player1, new DemonicHordes());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps the creature untapped")
    void payingUpkeepCostKeepsCreatureUntapped() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hordes.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("If unpaid without a land, only taps the creature")
    void unpaidUpkeepWithoutLandOnlyTapsCreature() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(hordes.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("If unpaid, taps and sacrifices the only land")
    void unpaidUpkeepSacrificesOnlyLand() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("If unpaid, an opponent chooses which land to sacrifice")
    void opponentChoosesLandToSacrifice() {
        Permanent hordes = addCreatureReady(player1, new DemonicHordes());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player2, firstLand.getId());

        assertThat(hordes.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstLand.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondLand.getId()));
    }
}
