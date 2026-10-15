package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CavernWhisperer;
import com.github.laxika.magicalvibes.cards.d.DreamtailHeron;
import com.github.laxika.magicalvibes.cards.d.DurableCoilbug;
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

@CardUsed({ZagothMamba.class, CavernWhisperer.class, DurableCoilbug.class, DreamtailHeron.class})
class ZagothMambaTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating gives target creature an opponent controls -2/-2 until end of turn")
    void mutatingShrinksTargetOpponentCreature() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());

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
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("The mutation trigger cannot target a creature the controller controls")
    void cannotTargetOwnCreature() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent ownBear = addCreatureReady(player1, new DurableCoilbug());
        addCreatureReady(player2, new CavernWhisperer());

        triggerMutation(mamba);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Casting normally does not shrink an opponent's creature")
    void castingNormallyDoesNotShrinkCreature() {
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());

        harness.castFromHand(player1, new ZagothMamba(), "{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Zagoth Mamba");
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reducing toughness to zero puts the target in its owner's graveyard")
    void mutationKillsTwoToughnessCreature() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent bear = addCreatureReady(player2, new DurableCoilbug());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Durable Coilbug");
        harness.assertInGraveyard(player2, "Durable Coilbug");
    }

    @Test
    @DisplayName("Each mutation applies another -2/-2")
    void repeatedMutationsStackTheirReductions() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cavern Whisperer");
        harness.assertInGraveyard(player2, "Cavern Whisperer");
    }

    @Test
    @DisplayName("The mutation trigger resolves after its source leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());

        triggerMutation(mamba);
        harness.handlePermanentChosen(player1, elemental.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mamba);
        gd.playerGraveyards.get(player1.getId()).add(mamba.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("A mutation with no legal opposing creature does not request a target")
    void noOpposingCreaturesDoesNotRequestTarget() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        addCreatureReady(player1, new DurableCoilbug());

        triggerMutation(mamba);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Zagoth Mamba");
        harness.assertOnBattlefield(player1, "Durable Coilbug");
    }

    @Test
    @DisplayName("Resolving a real mutating creature spell triggers Zagoth Mamba")
    void resolvingMutateSpellTriggersMamba() {
        Permanent mamba = addCreatureReady(player1, new ZagothMamba());
        Permanent elemental = addCreatureReady(player2, new CavernWhisperer());
        harness.setLibrary(player1, List.of(new DurableCoilbug()));
        harness.setHand(player1, List.of(new DreamtailHeron()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, mamba.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void triggerMutation(Permanent mamba) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, mamba, List.of(mamba.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
