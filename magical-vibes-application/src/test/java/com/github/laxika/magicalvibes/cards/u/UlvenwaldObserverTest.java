package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UlvenwaldObserverTest extends BaseCardTest {

    @Test
    void drawsWhenCreatureWithToughnessAtLeastFourYouControlDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GiantSpider());

        destroy(player1, player1, "Giant Spider");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenCreatureWithToughnessBelowFourYouControlDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroy(player1, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenUlvenwaldObserverItselfDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());

        destroy(player1, player1, "Ulvenwald Observer");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenOpponentsCreatureDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player2, new GiantSpider());

        destroy(player1, player2, "Giant Spider");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void destroy(Player caster, Player targetController, String targetName) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
