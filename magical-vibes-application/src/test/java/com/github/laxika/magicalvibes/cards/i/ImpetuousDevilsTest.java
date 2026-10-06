package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CatharsShield;
import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpetuousDevils.class, FieldCreeper.class, CatharsShield.class})
class ImpetuousDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers up to one creature defending player controls and forces it to block")
    void attackTriggerForcesChosenDefendingCreatureToBlock() {
        Permanent devils = addCreatureReady(player1, new ImpetuousDevils());
        Permanent defendingCreature = addCreatureReady(player2, new FieldCreeper());
        Permanent ownCreature = addCreatureReady(player1, new FieldCreeper());
        Permanent defendingNoncreature = harness.addToBattlefieldAndReturn(player2, new CatharsShield());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(defendingCreature.getId())
                .doesNotContain(devils.getId(), ownCreature.getId(), defendingNoncreature.getId());

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(defendingCreature.getRequiredBlockSourceIds()).containsExactly(devils.getId());
    }

    @Test
    @DisplayName("Declining the optional attack target does not impose a block requirement")
    void attackTriggerTargetCanBeDeclined() {
        Permanent devils = addCreatureReady(player1, new ImpetuousDevils());
        addCreatureReady(player2, new FieldCreeper());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Field Creeper");
        assertThat(blocker.getRequiredBlockSourceIds()).isEmpty();
        assertThat(devils.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The chosen creature must block the Devils when able")
    void chosenCreatureMustBlock() {
        Permanent devils = addCreatureReady(player1, new ImpetuousDevils());
        Permanent blocker = addCreatureReady(player2, new FieldCreeper());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(devils.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of the end step, the Devils are sacrificed")
    void sacrificesAtEndStep() {
        Permanent devils = addCreatureReady(player1, new ImpetuousDevils());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(devils.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Impetuous Devils");
        harness.assertInGraveyard(player1, "Impetuous Devils");
    }

    @Test
    @DisplayName("Freshly entered Devils can attack and trample over their required blocker")
    void hasteAndTrampleWorkInCombat() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImpetuousDevils());
        Permanent blocker = addCreatureReady(player2, new FieldCreeper());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Impetuous Devils");
        harness.assertInGraveyard(player2, "Field Creeper");
    }

    @Test
    @DisplayName("A tapped target need not block when unable")
    void tappedTargetIsNotRequiredToBlock() {
        addCreatureReady(player1, new ImpetuousDevils());
        Permanent blocker = addCreatureReady(player2, new FieldCreeper());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, blocker.getId());
            harness.passBothPriorities();
        });

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The block requirement expires before another combat in the same turn")
    void blockRequirementDoesNotCarryIntoAnotherCombat() {
        Permanent devils = addCreatureReady(player1, new ImpetuousDevils());
        Permanent blocker = addCreatureReady(player2, new FieldCreeper());
        blocker.tap();

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, blocker.getId());
            harness.passBothPriorities();
        });

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        devils.untap();
        blocker.untap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The Devils are sacrificed at an opponent's end step too")
    void sacrificesAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new ImpetuousDevils());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Impetuous Devils");
        harness.assertInGraveyard(player1, "Impetuous Devils");
    }
}
