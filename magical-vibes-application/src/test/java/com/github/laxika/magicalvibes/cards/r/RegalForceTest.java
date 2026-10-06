package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HelixPinnacle;
import com.github.laxika.magicalvibes.cards.s.ShorecrasherMimic;
import com.github.laxika.magicalvibes.cards.u.Unmake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalForce.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        HelixPinnacle.class, ShorecrasherMimic.class, Unmake.class})
class RegalForceTest extends BaseCardTest {

    private void stockDeckWithForests(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new Forest()).toList());
    }

    @Test
    @DisplayName("ETB draws a card for each green creature, counting itself")
    void etbDrawsForEachGreenCreatureIncludingItself() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new RegalForce()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Two Grizzly Bears + Regal Force itself = 3 green creatures.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Non-green creatures are not counted")
    void nonGreenCreaturesNotCounted() {
        addCreatureReady(player1, new HillGiant());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new RegalForce()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Only Regal Force itself is green.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1 + 1);
    }

    @Test
    @DisplayName("Counts multicolored green creatures but excludes opponents and green noncreatures")
    void countsOnlyControlledGreenCreatures() {
        addCreatureReady(player1, new ShorecrasherMimic());
        addCreatureReady(player2, new ShorecrasherMimic());
        harness.addToBattlefield(player1, new HelixPinnacle());
        stockDeckWithForests(5);
        harness.setHand(player1, List.of(new RegalForce()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Trigger survives removal and counts only green creatures still controlled at resolution")
    void drawsForRemainingCreaturesAfterSourceIsExiled() {
        addCreatureReady(player1, new ShorecrasherMimic());
        stockDeckWithForests(5);
        RegalForce force = new RegalForce();
        harness.setHand(player1, List.of(force, new Unmake()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Regal Force"));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(force.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws zero when the only green creature leaves before the trigger resolves")
    void drawsZeroAfterOnlyGreenCreatureIsExiled() {
        stockDeckWithForests(5);
        RegalForce force = new RegalForce();
        harness.setHand(player1, List.of(force, new Unmake()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Regal Force"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
