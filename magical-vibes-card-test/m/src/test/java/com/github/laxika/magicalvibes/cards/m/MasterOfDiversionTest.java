package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfDiversion.class, GrizzlyBears.class})
class MasterOfDiversionTest extends BaseCardTest {

    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({MasterOfDiversion.class, GrizzlyBears.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking queues the attack trigger for target selection")
        void queuesTargetSelection() {
            addCreatureReady(player1, new MasterOfDiversion());
            harness.addToBattlefield(player2, new GrizzlyBears());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Resolving the trigger taps the defending player's creature")
        void tapsDefendingCreature() {
            addCreatureReady(player1, new MasterOfDiversion());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Own creatures are not tapped by the trigger")
        void leavesOwnCreatureUntapped() {
            addCreatureReady(player1, new MasterOfDiversion());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent ownBears = findPermanent(player1, "Grizzly Bears");
            Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, opponentBears.getId());
            harness.passBothPriorities();

            assertThat(opponentBears.isTapped()).isTrue();
            assertThat(ownBears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("No trigger target interaction when the defender controls no creatures")
        void noInteractionWithoutLegalTarget() {
            addCreatureReady(player1, new MasterOfDiversion());

            declareAttackers(List.of(0));

            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        }
    }

    @Test
    @DisplayName("An already tapped defending creature remains a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new MasterOfDiversion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfDiversion());
        target.tap();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger taps only the chosen defending creature")
    void tapsOnlyChosenCreature() {
        addCreatureReady(player1, new MasterOfDiversion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfDiversion());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new MasterOfDiversion());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Master that does not attack does not trigger")
    void doesNotTriggerWhenNotAttacking() {
        addCreatureReady(player1, new MasterOfDiversion());
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new MasterOfDiversion());

        declareAttackers(List.of());

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(defender.isTapped()).isFalse();
    }
}
