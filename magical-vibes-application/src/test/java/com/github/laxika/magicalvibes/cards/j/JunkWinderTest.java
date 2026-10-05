package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NestedShambler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkWinder.class, CallTheCavalry.class, NestedShambler.class, Forest.class})
class JunkWinderTest extends BaseCardTest {

    @Test
    @DisplayName("Token affinity reduces Junk Winder's generic cost")
    void tokenAffinityReducesGenericCost() {
        castCallTheCavalry(player1);

        harness.setHand(player1, List.of(new JunkWinder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each token entering triggers an independently targeted lock")
    void tokenEntryTapsAndLocksTargetedNonlandPermanent() {
        Permanent junkWinder = addCreatureReady(player1, new JunkWinder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NestedShambler());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new NestedShambler());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castCallTheCavalry(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), secondTarget.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(forest.getId(), junkWinder.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(secondTarget.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
        assertThat(secondTarget.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
        assertThat(secondTarget.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent tokens do not trigger Junk Winder")
    void opponentTokensDoNotTrigger() {
        addCreatureReady(player1, new JunkWinder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NestedShambler());

        castCallTheCavalry(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("A nontoken creature entering does not trigger Junk Winder")
    void nontokenEntryDoesNotTrigger() {
        addCreatureReady(player1, new JunkWinder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NestedShambler());

        harness.castFromHand(player1, new NestedShambler(), "{B}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Affinity ignores opponent tokens and your nontoken permanents")
    void affinityCountsOnlyYourTokens() {
        castCallTheCavalry(player2);
        harness.addToBattlefield(player1, new NestedShambler());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new JunkWinder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity can remove all generic mana but leaves the blue cost")
    void affinityStopsAtColoredCost() {
        castCallTheCavalry(player1);
        castCallTheCavalry(player1);
        castCallTheCavalry(player1);
        harness.setHand(player1, List.of(new JunkWinder()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void castCallTheCavalry(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new CallTheCavalry(), "{3}{W}");
        resolveAllTriggers();
    }
}
