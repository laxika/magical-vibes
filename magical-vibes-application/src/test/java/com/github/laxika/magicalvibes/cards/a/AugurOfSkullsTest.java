package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AugurOfSkulls.class})
class AugurOfSkullsTest extends BaseCardTest {

    @Test
    @DisplayName("The regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new AugurOfSkulls());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(augur.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield prevents lethal damage from destroying Augur of Skulls")
    void regenerationShieldPreventsLethalDamage() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new AugurOfSkulls());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        augur.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(augur);
        assertThat(augur.getRegenerationShield()).isZero();
        assertThat(augur.getMarkedDamage()).isZero();
        assertThat(augur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The sacrifice ability makes the target player discard two cards")
    void sacrificeAbilityDiscardsTwoCards() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new AugurOfSkulls(), new AugurOfSkulls(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Augur of Skulls");
        harness.assertInGraveyard(player1, "Augur of Skulls");
    }

    @Test
    @DisplayName("The sacrifice ability can target its controller")
    void sacrificeAbilityCanTargetItsController() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player1, List.of(new AugurOfSkulls(), new AugurOfSkulls(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Augur of Skulls");
    }

    @Test
    @DisplayName("The sacrifice ability can only be activated during its controller's upkeep")
    void sacrificeAbilityOnlyWorksDuringUpkeep() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The sacrifice ability cannot be activated during an opponent's upkeep")
    void sacrificeAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The sacrifice ability only accepts a player target")
    void sacrificeAbilityRejectsPermanentTarget() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new AugurOfSkulls());
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, augur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("A player with only one card discards it and the ability finishes")
    void sacrificeAbilityDiscardsAvailableCard() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand is a legal target and sacrifice is paid before resolution")
    void sacrificeAbilityTargetsEmptyHand() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Augur of Skulls");
        harness.assertInGraveyard(player1, "Augur of Skulls");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield cannot prevent paying the sacrifice cost")
    void regenerationDoesNotPreventSacrifice() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Augur of Skulls");
        harness.assertInGraveyard(player1, "Augur of Skulls");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped during an opponent's upkeep")
    void regenerationWorksWhileTappedDuringOpponentsUpkeep() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new AugurOfSkulls());
        advanceToUpkeep(player2);
        augur.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(augur.getRegenerationShield()).isEqualTo(1);
        assertThat(augur.isTapped()).isTrue();
    }

}
