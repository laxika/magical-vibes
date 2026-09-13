package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GerrardCapashen.class, GaeasSkyfolk.class})
class GerrardCapashenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to target opponent's hand size on upkeep")
    void gainsLifeEqualToTargetOpponentsHandSize() {
        harness.addToBattlefield(player1, new GerrardCapashen());
        harness.setHand(player2, List.of(new GaeasSkyfolk(), new GaeasSkyfolk(), new GaeasSkyfolk()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Gains no life when the target opponent's hand is empty")
    void gainsNoLifeWhenTargetOpponentsHandIsEmpty() {
        harness.addToBattlefield(player1, new GerrardCapashen());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts the target opponent's hand when the trigger resolves")
    void countsTargetOpponentsHandAtResolution() {
        harness.addToBattlefield(player1, new GerrardCapashen());
        harness.setHand(player2, List.of(new GaeasSkyfolk(), new GaeasSkyfolk(), new GaeasSkyfolk()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new GaeasSkyfolk()));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Upkeep trigger can only target an opponent")
    void upkeepTriggerCanOnlyTargetOpponent() {
        harness.addToBattlefield(player1, new GerrardCapashen());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Can tap a creature while attacking")
    void tapsCreatureWhileAttacking() {
        addCreatureReady(player1, new GerrardCapashen());
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());
        declareAttackers(List.of(0));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while not attacking")
    void cannotActivateTapAbilityWhileNotAttacking() {
        addCreatureReady(player1, new GerrardCapashen());
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }
}
