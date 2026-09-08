package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShessraDeathsWhisper.class, GrizzlyBears.class, Forest.class})
class ShessraDeathsWhisperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB forces the target creature to block this turn when able")
    void etbForcesTargetCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castShessraTargeting(blocker);

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ETB does not require a tapped target creature to block")
    void etbDoesNotRequireTappedCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castShessraTargeting(blocker);

        blocker.tap();
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ETB can target only a creature")
    void etbCannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ShessraDeathsWhisper()));
        addManaForShessra();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("At the end step, paying 2 life draws a card after a creature dies")
    void paysLifeToDrawAfterCreatureDies() {
        harness.addToBattlefield(player1, new ShessraDeathsWhisper());
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dyingCreature));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Declining the end-step payment does not draw or lose life")
    void declinesEndStepPayment() {
        harness.addToBattlefield(player1, new ShessraDeathsWhisper());
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dyingCreature));
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("The end-step ability does not trigger when no creature died")
    void doesNotTriggerWithoutCreatureDeath() {
        harness.addToBattlefield(player1, new ShessraDeathsWhisper());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    private void castShessraTargeting(Permanent target) {
        harness.setHand(player1, List.of(new ShessraDeathsWhisper()));
        addManaForShessra();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForShessra() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
