package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkWinder.class, CallTheCavalry.class, GrizzlyBears.class, Forest.class})
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
    @DisplayName("A token entering triggers one target lock for the batch")
    void tokenEntryTapsAndLocksTargetedNonlandPermanent() {
        Permanent junkWinder = addCreatureReady(player1, new JunkWinder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castCallTheCavalry(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(bears.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(forest.getId(), junkWinder.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    private void castCallTheCavalry(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new CallTheCavalry()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castSorcery(player, 0, 0);
        resolveAllTriggers();
    }
}
