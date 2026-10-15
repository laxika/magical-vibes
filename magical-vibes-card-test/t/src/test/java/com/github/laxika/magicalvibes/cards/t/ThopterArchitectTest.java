package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThopterArchitect.class, GrizzlyBears.class, Ornithopter.class, MycosynthLattice.class})
class ThopterArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under its controller's control queues a creature target")
    void artifactEnterQueuesTargetChoice() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
    }

    @Test
    @DisplayName("The chosen creature gains flying until end of turn")
    void chosenCreatureGainsFlying() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The trigger cannot target a player")
    void triggerCannotTargetPlayer() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Thopter Architect")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThopterArchitect());

        harness.enterBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger can grant flying to an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThopterArchitect());

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Thopter Architect can target itself")
    void canTargetItself() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new ThopterArchitect());

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, architect.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, architect, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not trigger the ability")
    void nonartifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThopterArchitect());

        harness.castFromHand(player1, new ThopterArchitect(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new ThopterArchitect());
        Permanent lattice = harness.addToBattlefieldAndReturn(player1, new MycosynthLattice());

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lattice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature artifact entering also triggers the ability")
    void noncreatureArtifactTriggers() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new ThopterArchitect());

        harness.castFromHand(player1, new MycosynthLattice(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, architect.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, architect, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Thopter Architect triggers for its own entry when it enters as an artifact")
    void artifactArchitectTriggersForItsOwnEntry() {
        harness.addToBattlefield(player1, new MycosynthLattice());

        harness.castFromHand(player1, new ThopterArchitect(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Thopter Architect"));
        harness.passBothPriorities();

        Permanent architect = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ThopterArchitect)
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, architect, Keyword.FLYING)).isTrue();
    }
}
