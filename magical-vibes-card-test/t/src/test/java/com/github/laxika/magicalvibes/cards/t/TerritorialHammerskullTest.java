package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.p.PillarOfOrigins;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialHammerskull.class, RaptorCompanion.class, PillarOfOrigins.class})
class TerritorialHammerskullTest extends BaseCardTest {

    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({TerritorialHammerskull.class, RaptorCompanion.class, PillarOfOrigins.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking queues attack trigger for target selection")
        void attackTriggerQueuesForTargetSelection() {
            addCreatureReady(player1, new TerritorialHammerskull());
            harness.addToBattlefield(player2, new RaptorCompanion());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Resolving attack trigger taps target creature")
        void attackTriggerTapsTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            assertThat(bears.isTapped()).isFalse();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Does not tap own creatures — only opponent's creatures are valid targets")
        @CardUsed({TerritorialHammerskull.class, RaptorCompanion.class, PillarOfOrigins.class})
        void cannotTargetOwnCreatures() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
            Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PillarOfOrigins());
            artifact.setChosenSubtype(CardSubtype.DINOSAUR);

            declareAttackers(List.of(0));
            PendingInteraction.PermanentChoice choice =
                    (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
            assertThat(choice.validPermanentIds()).containsExactly(opponentBears.getId());
            harness.handlePermanentChosen(player1, opponentBears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // Opponent creature is tapped, own creature is not
            assertThat(opponentBears.isTapped()).isTrue();
            assertThat(ownBears.isTapped()).isFalse();
            assertThat(artifact.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Already tapped creature can still be targeted")
        void canTargetAlreadyTappedCreature() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            bears.tap();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Attack trigger puts triggered ability on the stack")
        void attackTriggerPutsOnStack() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Territorial Hammerskull");
        }

        @Test
        @DisplayName("Attacking without opposing creatures requires no target choice")
        void noLegalTarget() {
            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.stack).isEmpty();
            assertThat(ownCreature.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Trigger still taps its target after Hammerskull leaves the battlefield")
        void sourceLeavingDoesNotStopTrigger() {
            Permanent source = addCreatureReady(player1, new TerritorialHammerskull());
            Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());

            gd.playerBattlefields.get(player1.getId()).remove(source);
            gd.playerGraveyards.get(player1.getId()).add(source.getCard());
            harness.passBothPriorities();

            assertThat(target.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target that becomes controlled by the attacker is no longer legal")
        void targetBecomingFriendlyIsNotTapped() {
            addCreatureReady(player1, new TerritorialHammerskull());
            Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());

            gd.playerBattlefields.get(player2.getId()).remove(target);
            gd.playerBattlefields.get(player1.getId()).add(target);
            harness.passBothPriorities();

            assertThat(target.isTapped()).isFalse();
        }
    }

}
