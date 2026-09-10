package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostmortemProfessorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes each opponent lose 1 life and its controller gain 1 life")
    void attackLifeSwing() {
        PostmortemProfessor card = new PostmortemProfessor();
        card.setPower(0);
        addCreatureReady(player1, card);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The professor cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent professor = addCreatureReady(player2, new PostmortemProfessor());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(professor);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling an instant from the graveyard returns the professor to the battlefield")
    void exilesInstantAndReturnsToBattlefield() {
        PostmortemProfessor professor = new PostmortemProfessor();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(professor, shock));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Postmortem Professor");
        harness.assertNotInGraveyard(player1, "Postmortem Professor");
    }

    @Test
    @DisplayName("The graveyard ability requires an instant or sorcery card")
    void requiresInstantOrSorceryInGraveyard() {
        harness.setGraveyard(player1, List.of(new PostmortemProfessor(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
    }
}
