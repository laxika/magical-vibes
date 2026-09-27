package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FightOrFlight.class, RagingKavu.class, RazorfootGriffin.class})
class FightOrFlightTest extends BaseCardTest {

    private void advanceToOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("separates the active opponent's creatures at the beginning of combat")
    void separatesActiveOpponentsCreatures() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());

        advanceToOpponentCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(kavu.getId());
    }

    @Test
    @DisplayName("lets only the chosen pile attack without forcing those creatures to attack")
    void chosenPileIsTheOnlyAttackablePile() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        Permanent griffin = addCreatureReady(player2, new RazorfootGriffin());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getCombatAttackService().getMustAttackIndices(gd, player2.getId(), List.of(0)))
                .isEmpty();
        assertThat(harness.getAttackLegalityService().canAttack(gd, griffin, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("the opponent may choose the other pile")
    void opponentMayChoosePileTwo() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        addCreatureReady(player2, new RazorfootGriffin());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, false);

        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .containsExactly(1);
        assertThat(harness.getAttackLegalityService().canAttack(gd, kavu, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("allows an empty pile, leaving all creatures unable to attack")
    void emptyChosenPileBarsAllCreatures() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());
        Permanent griffin = addCreatureReady(player2, new RazorfootGriffin());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .isEmpty();
        assertThat(harness.getAttackLegalityService().canAttack(gd, kavu, player2.getId())).isFalse();
        assertThat(harness.getAttackLegalityService().canAttack(gd, griffin, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("does not let a creature that enters after the split attack")
    void creatureEnteringAfterSplitCannotAttack() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent kavu = addCreatureReady(player2, new RagingKavu());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, true);

        Permanent laterCreature = addCreatureReady(player2, new RagingKavu());
        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getAttackLegalityService().canAttack(gd, laterCreature, player2.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("does not prompt when the active opponent controls no creatures")
    void doesNotPromptWithoutCreatures() {
        harness.addToBattlefield(player1, new FightOrFlight());
        harness.addToBattlefield(player2, new FightOrFlight());

        advanceToOpponentCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("does not trigger on its controller's turn")
    void doesNotTriggerOnControllersTurn() {
        harness.addToBattlefield(player1, new FightOrFlight());
        addCreatureReady(player1, new RagingKavu());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AttackerDeclaration.class);
    }
}
