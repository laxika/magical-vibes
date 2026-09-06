package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ZagothMamba.class, AirElemental.class, GrizzlyBears.class})
class ZagothMambaTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating gives target creature an opponent controls -2/-2 until end of turn")
    void mutatingShrinksTargetOpponentCreature() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("The mutation shrink wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("The mutation trigger cannot target a creature the controller controls")
    void cannotTargetOwnCreature() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AirElemental());

        triggerMutation(mamba);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void triggerMutation(Permanent mamba) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, mamba, List.of(mamba.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
