package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MossPitSkeleton.class, BondBeetle.class, GrizzlyBears.class})
class MossPitSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Kicked Moss-Pit Skeleton enters with three +1/+1 counters")
    void kickedEntersWithThreeCounters() {
        harness.setHand(player1, List.of(new MossPitSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Moss-Pit Skeleton");
        assertThat(skeleton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("While in the graveyard, Moss-Pit Skeleton may go on top after a controlled creature gets a counter")
    void graveyardTriggerPutsItOnTop() {
        MossPitSkeleton skeleton = new MossPitSkeleton();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(skeleton));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(skeleton);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(skeleton);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A counter on an opponent's creature does not trigger Moss-Pit Skeleton")
    void opponentCreatureDoesNotTrigger() {
        MossPitSkeleton skeleton = new MossPitSkeleton();
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(skeleton));
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.getGameService().playCard(gd, player1, 0, 0, opposingBears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skeleton);
    }
}
