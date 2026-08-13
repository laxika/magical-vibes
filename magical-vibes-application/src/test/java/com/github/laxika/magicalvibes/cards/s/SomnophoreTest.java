package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SomnophoreTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to a player prompts for a creature that player controls")
    void promptsForDamagedPlayersCreature() {
        Permanent somnophore = addCreatureReady(player1, new Somnophore());
        somnophore.setAttacking(true);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent controllersCreature = addCreatureReady(player1, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId())
                .doesNotContain(controllersCreature.getId());
    }

    @Test
    @DisplayName("The chosen creature stays tapped while Somnophore remains on the battlefield")
    void chosenCreatureStaysTappedUntilSourceLeaves() {
        Permanent somnophore = addCreatureReady(player1, new Somnophore());
        somnophore.setAttacking(true);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(damagedPlayersCreature.getId()));

        assertThat(damagedPlayersCreature.isTapped()).isTrue();
        assertThat(damagedPlayersCreature.getUntapPreventedWhileSourceOnBattlefieldIds())
                .contains(somnophore.getId());

        advanceToNextTurn(player1);
        assertThat(damagedPlayersCreature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(somnophore);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(damagedPlayersCreature.isTapped()).isFalse();
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
}
