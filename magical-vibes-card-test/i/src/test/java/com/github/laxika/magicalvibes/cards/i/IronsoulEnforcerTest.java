package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronsoulEnforcer.class, FountainOfYouth.class, GrizzlyBears.class})
class IronsoulEnforcerTest extends BaseCardTest {

    @Test
    void returnsArtifactWhenItAttacksAlone() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        chooseArtifactAndResolve(artifact);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void returnsArtifactWhenCommanderAttacksAlone() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        chooseArtifactAndResolve(artifact);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void doesNotTriggerWhenAttackingWithAnotherCreature() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void doesNotTriggerForNoncommanderAttacker() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void commanderAttackTriggerHasEnforcerAsItsSource() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        var enforcer = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(enforcer.getId());
    }

    @Test
    void twoEnforcersCreateSeparateTriggersWithIndependentTargets() {
        FountainOfYouth firstArtifact = new FountainOfYouth();
        FountainOfYouth secondArtifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        addCreatureReady(player1, new IronsoulEnforcer());
        addCreatureReady(player1, new IronsoulEnforcer());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(2)));
        harness.handleMultipleCardsChosen(player1, List.of(firstArtifact.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondArtifact.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(firstArtifact.getId()))
                .anyMatch(p -> p.getCard().getId().equals(secondArtifact.getId()));
    }

    @Test
    void cannotTargetNonartifactOrOpponentsArtifact() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new FountainOfYouth()));
        addCreatureReady(player1, new IronsoulEnforcer());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void returnsArtifactCreature() {
        IronsoulEnforcer artifact = new IronsoulEnforcer();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        chooseArtifactAndResolve(artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artifact.getId()));
        harness.assertNotInGraveyard(player1, "Ironsoul Enforcer");
    }

    @Test
    void commanderEnforcerTriggersOnlyOnceWhenItAttacksAlone() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        IronsoulEnforcer commander = new IronsoulEnforcer();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        addCreatureReady(player1, new IronsoulEnforcer());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    private void chooseArtifactAndResolve(Card artifact) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
    }

}
