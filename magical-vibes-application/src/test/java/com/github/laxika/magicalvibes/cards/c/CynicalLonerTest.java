package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CynicalLoner.class, GrizzlyBears.class})
class CynicalLonerTest extends BaseCardTest {

    @Test
    void acceptedSurvivalSearchPutsCardIntoGraveyard() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void declinedSurvivalSearchLeavesLibraryUnchanged() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void untappedLonerDoesNotTriggerSurvival() {
        harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotBeBlockedByGlimmer() {
        Permanent blocker = new Permanent(glimmerCreature());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent loner = new Permanent(new CynicalLoner());
        loner.setSummoningSick(false);
        loner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(loner);

        prepareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loner);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByNonGlimmer() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent loner = new Permanent(new CynicalLoner());
        loner.setSummoningSick(false);
        loner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(loner);

        prepareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loner);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Card glimmerCreature() {
        Card glimmer = new Card();
        glimmer.setName("Glimmer");
        glimmer.setType(CardType.CREATURE);
        glimmer.setSubtypes(List.of(CardSubtype.GLIMMER));
        glimmer.setPower(1);
        glimmer.setToughness(1);
        return glimmer;
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
