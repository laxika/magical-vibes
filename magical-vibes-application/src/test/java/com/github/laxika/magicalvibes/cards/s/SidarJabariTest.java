package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SidarJabariTest extends BaseCardTest {

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking queues the attack trigger for target selection")
        void queuesTargetSelection() {
            addReadyJabari(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Resolving the trigger taps the defending player's creature")
        void tapsDefendingCreature() {
            addReadyJabari(player1);
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
            addReadyJabari(player1);
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
            addReadyJabari(player1);

            declareAttackers(List.of(0));

            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        }
    }

    private void addReadyJabari(Player player) {
        Permanent perm = new Permanent(new SidarJabari());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
