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

    @Test
    @DisplayName("a defender may leave the left pile empty")
    void allowsEmptyLeftPile() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ground = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SerraAngel());

        resolveRagingRiver(attacker);
        harness.handleMultiplePermanentsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, ground, attacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyer, attacker, defenderBattlefield)).isTrue();
    }

    @Test
    @DisplayName("creatures entering after resolution are not added to either pile")
    void excludesNewGroundBlockers() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ground = addCreatureReady(player2, new GrizzlyBears());

        resolveRagingRiver(attacker);
        harness.handleMultiplePermanentsChosen(player2, List.of(ground.getId()));
        harness.handleMayAbilityChosen(player1, true);
        Permanent newcomer = addCreatureReady(player2, new GrizzlyBears());

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, ground, attacker, defenderBattlefield)).isTrue();
        assertThat(bls.canBlockAttacker(gd, newcomer, attacker, defenderBattlefield)).isFalse();
    }

    @Test
    @DisplayName("left or right is still chosen when the defender has only flyers")
    void handlesNoGroundCreatures() {
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SerraAngel());

        resolveRagingRiver(attacker);
        harness.handleMayAbilityChosen(player1, false);
        Permanent newcomer = addCreatureReady(player2, new GrizzlyBears());

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, flyer, attacker, defenderBattlefield)).isTrue();
        assertThat(bls.canBlockAttacker(gd, newcomer, attacker, defenderBattlefield)).isFalse();
    }

    @Test
    @DisplayName("blockers must satisfy the chosen piles of both Raging Rivers")
    void combinesMultipleRiverRestrictions() {
        harness.addToBattlefield(player1, new RagingRiver());
        harness.addToBattlefield(player1, new RagingRiver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstGround = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondGround = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyer = addCreatureReady(player2, new SerraAngel());

        resolveRagingRiver(attacker);
        harness.handleMultiplePermanentsChosen(player2, List.of(firstGround.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(secondGround.getId()));
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, firstGround, attacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, secondGround, attacker, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyer, attacker, defenderBattlefield)).isTrue();
    }
}
