package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AugurOfSkulls.class, GrizzlyBears.class})
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
    @DisplayName("The sacrifice ability makes the target player discard two cards")
    void sacrificeAbilityDiscardsTwoCards() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        beginUpkeep();

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

    private void beginUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
