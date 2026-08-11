package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FightOrFlightTest extends BaseCardTest {

    private void advanceToOpponentCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToOpponentCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("lets only the chosen pile attack without forcing those creatures to attack")
    void chosenPileIsTheOnlyAttackablePile() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .containsExactly(0);
        assertThat(harness.getCombatAttackService().getMustAttackIndices(gd, player2.getId(), List.of(0)))
                .isEmpty();
        assertThat(harness.getAttackLegalityService().canAttack(gd, spider, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("the opponent may choose the other pile")
    void opponentMayChoosePileTwo() {
        harness.addToBattlefield(player1, new FightOrFlight());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        advanceToOpponentCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, false);

        beginDeclareAttackers();

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .containsExactly(1);
        assertThat(harness.getAttackLegalityService().canAttack(gd, bears, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("does not trigger on its controller's turn")
    void doesNotTriggerOnControllersTurn() {
        harness.addToBattlefield(player1, new FightOrFlight());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }
}
