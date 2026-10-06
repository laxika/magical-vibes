package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidarJabari.class, BayFalcon.class})
class SidarJabariTest extends BaseCardTest {

    @Nested
    @CardUsed({SidarJabari.class, BayFalcon.class})
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking queues the attack trigger for target selection")
        void queuesTargetSelection() {
            addReadyJabari(player1);
            harness.addToBattlefield(player2, new BayFalcon());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Resolving the trigger taps the defending player's creature")
        void tapsDefendingCreature() {
            addReadyJabari(player1);
            Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, falcon.getId());
            harness.passBothPriorities();

            assertThat(falcon.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Own creatures are not tapped by the trigger")
        void leavesOwnCreatureUntapped() {
            addReadyJabari(player1);
            Permanent ownFalcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
            Permanent opponentFalcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, opponentFalcon.getId());
            harness.passBothPriorities();

            assertThat(opponentFalcon.isTapped()).isTrue();
            assertThat(ownFalcon.isTapped()).isFalse();
        }

        @Test
        @DisplayName("A creature I control is not a legal target")
        void rejectsOwnCreatureAsTarget() {
            addReadyJabari(player1);
            Permanent ownFalcon = addCreatureReady(player1, new BayFalcon());
            Permanent opponentFalcon = addCreatureReady(player2, new BayFalcon());

            declareAttackers(List.of(0));

            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownFalcon.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent");

            harness.handlePermanentChosen(player1, opponentFalcon.getId());
            harness.passBothPriorities();

            assertThat(ownFalcon.isTapped()).isFalse();
            assertThat(opponentFalcon.isTapped()).isTrue();
        }

        @Test
        @DisplayName("No trigger target interaction when the defender controls no creatures")
        void noInteractionWithoutLegalTarget() {
            addReadyJabari(player1);

            declareAttackers(List.of(0));

            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        }

        @Test
        @DisplayName("An already-tapped creature remains a legal attack-trigger target")
        void canTargetTappedCreature() {
            addReadyJabari(player1);
            Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
            falcon.setTapped(true);

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, falcon.getId());
            harness.passBothPriorities();

            assertThat(falcon.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("The attack trigger resolves after Jabari leaves the battlefield")
        void triggerSurvivesSourceLeaving() {
            Permanent jabari = addCreatureReady(player1, new SidarJabari());
            Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, falcon.getId());
            gd.playerBattlefields.get(player1.getId()).remove(jabari);
            gd.playerGraveyards.get(player1.getId()).add(jabari.getCard());
            harness.passBothPriorities();

            assertThat(falcon.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("A blocker with flanking does not trigger Jabari's flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new SidarJabari());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SidarJabari());
        int powerBeforeBlocking = gqs.getEffectivePower(gd, blocker);
        int toughnessBeforeBlocking = gqs.getEffectiveToughness(gd, blocker);

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(powerBeforeBlocking);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(toughnessBeforeBlocking);
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new SidarJabari());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BayFalcon());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    private void addReadyJabari(Player player) {
        addCreatureReady(player, new SidarJabari());
    }
}
