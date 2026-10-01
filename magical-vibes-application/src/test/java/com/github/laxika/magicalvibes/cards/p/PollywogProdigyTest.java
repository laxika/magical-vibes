package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PollywogProdigy.class, GrizzlyBears.class, Forest.class, Shock.class, Memnite.class})
class PollywogProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Evolves when a creature with greater power enters under your control")
    void evolvesForGreaterPower() {
        Permanent prodigy = harness.addToBattlefieldAndReturn(player1, new PollywogProdigy());

        castGrizzlyBears();

        assertThat(prodigy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws when an opponent casts a noncreature spell with lower mana value")
    void drawsForQualifyingOpponentSpell() {
        harness.addToBattlefield(player1, new PollywogProdigy());
        castGrizzlyBears();

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareOpponentMainPhase();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawn);
    }

    @Test
    @DisplayName("Does not draw when the spell's mana value equals its power")
    void doesNotDrawForEqualManaValue() {
        harness.addToBattlefield(player1, new PollywogProdigy());

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareOpponentMainPhase();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Does not draw for a creature spell even when its mana value is lower")
    void doesNotDrawForCreatureSpell() {
        harness.addToBattlefield(player1, new PollywogProdigy());
        castGrizzlyBears();

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Memnite()));
        prepareOpponentMainPhase();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
