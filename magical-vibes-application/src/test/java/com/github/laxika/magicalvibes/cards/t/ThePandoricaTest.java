package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThePandorica.class, GrizzlyBears.class, Disenchant.class, Island.class})
class ThePandoricaTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and phases out another target nonland permanent")
    void untapsAndPhasesOutTarget() {
        Permanent pandorica = harness.addToBattlefieldAndReturn(player1, new ThePandorica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        addActivationMana();

        activate(pandorica, target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
        assertThat(pandorica.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Keeps the target phased out while The Pandorica remains tapped")
    void holdsTargetWhileTapped() {
        Permanent pandorica = harness.addToBattlefieldAndReturn(player1, new ThePandorica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        activate(pandorica, target);
        advanceToNextTurn(player1);

        assertThat(pandorica.isTapped()).isTrue();
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Phases the target in when The Pandorica becomes untapped")
    void phasesInWhenUntapped() {
        Permanent pandorica = harness.addToBattlefieldAndReturn(player1, new ThePandorica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        activate(pandorica, target);
        advanceToNextTurnWithMayChoice(player2, true);
        resolveAllTriggers();

        assertThat(pandorica.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Phases the target in when The Pandorica leaves the battlefield")
    void phasesInWhenSourceLeaves() {
        Permanent pandorica = harness.addToBattlefieldAndReturn(player1, new ThePandorica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();
        activate(pandorica, target);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, pandorica.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent pandorica = harness.addToBattlefieldAndReturn(player1, new ThePandorica());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        addActivationMana();

        assertThatThrownBy(() -> activate(pandorica, island))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    private void activate(Permanent pandorica, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pandorica);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
