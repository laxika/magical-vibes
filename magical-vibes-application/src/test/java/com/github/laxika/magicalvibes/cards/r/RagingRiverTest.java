package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagingRiver.class, GrizzlyBears.class, SerraAngel.class})
class RagingRiverTest extends BaseCardTest {

    private void resolveRagingRiver(Permanent... attackers) {
        List<Integer> attackerIndices = java.util.Arrays.stream(attackers)
                .map(attacker -> gd.playerBattlefields.get(player1.getId()).indexOf(attacker))
                .toList();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, attackerIndices));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("asks each defending player to divide their nonflying creatures")
    void asksForNonflyingCreaturePiles() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ground = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SerraAngel());

        resolveRagingRiver(attacker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(ground.getId());
        assertThat(choice.validIds()).doesNotContain(flyer.getId());
    }

    @Test
    @DisplayName("allows chosen-pile creatures and flyers to block, but not the other pile")
    void appliesChosenPileRestriction() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent left = addCreatureReady(player2, new GrizzlyBears());
        Permanent right = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SerraAngel());

        resolveRagingRiver(attacker);
        harness.handleMultiplePermanentsChosen(player2, List.of(left.getId()));
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, left, attacker, defenderBattlefield)).isTrue();
        assertThat(bls.canBlockAttacker(gd, right, attacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyer, attacker, defenderBattlefield)).isTrue();
    }

    @Test
    @DisplayName("chooses a left or right pile separately for each attacker")
    void choosesPerAttacker() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent left = addCreatureReady(player2, new GrizzlyBears());
        Permanent right = addCreatureReady(player2, new GrizzlyBears());

        resolveRagingRiver(firstAttacker, secondAttacker);
        harness.handleMultiplePermanentsChosen(player2, List.of(left.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, left, firstAttacker, defenderBattlefield)).isTrue();
        assertThat(bls.canBlockAttacker(gd, right, firstAttacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, left, secondAttacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, right, secondAttacker, defenderBattlefield)).isTrue();
    }
}
