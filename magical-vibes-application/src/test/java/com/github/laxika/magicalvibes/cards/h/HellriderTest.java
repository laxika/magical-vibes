package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class HellriderTest extends BaseCardTest {

    

    @Test
    @DisplayName("Triggers once for each creature you control that attacks")
    void triggersForEachAttackingCreature() {
        harness.setLife(player2, 20);
        Permanent hellrider = addReady(player1, new Hellrider());
        Permanent bears = addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1), null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Hellrider");
            assertThat(entry.getSourcePermanentId()).isEqualTo(gd.playerBattlefields.get(player1.getId()).getFirst().getId());
            // The triggering attacker is recorded as a non-targeting reference
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getAttackedTargetId()).isEqualTo(player2.getId());
        });
        // One trigger per attacking creature, each referencing its own attacker
        assertThat(gd.stack).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(hellrider.getId(), bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Does not trigger for opponent's attacking creatures")
    void doesNotTriggerForOpponentCreatures() {
        addReady(player1, new Hellrider());
        addReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Hellrider")))
                .isEmpty();
    }

    @Test
    @DisplayName("Damages the planeswalker an attacking creature is attacking")
    void damagesAttackedPlaneswalker() {
        addReady(player1, new Hellrider());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        // The triggering attacker rides along as a non-targeting reference
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.stack.getFirst().getAttackedTargetId()).isEqualTo(planeswalker.getId());

        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
