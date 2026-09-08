package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RigoStreetwiseMentor.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class RigoStreetwiseMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent rigo = castRigo();

        assertThat(rigo.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws when one or more creatures with power 1 or less attack")
    void drawsForQualifyingAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only one card for multiple qualifying attackers")
    void drawsOnlyOnceForMultipleQualifyingAttackers() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when no attacking creature has power 1 or less")
    void doesNotDrawForNonQualifyingAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent castRigo() {
        harness.setHand(player1, List.of(new RigoStreetwiseMentor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Rigo, Streetwise Mentor");
    }
}
