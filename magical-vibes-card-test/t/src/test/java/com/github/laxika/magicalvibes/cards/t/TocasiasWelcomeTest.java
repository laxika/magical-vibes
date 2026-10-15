package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AwakenTheWoods;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SteelSeraph;
import com.github.laxika.magicalvibes.cards.s.SurvivorOfKorlis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TocasiasWelcome.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        AwakenTheWoods.class, SurvivorOfKorlis.class, SteelSeraph.class})
class TocasiasWelcomeTest extends BaseCardTest {

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Forest()).toList());
    }

    @Test
    @DisplayName("Draws when a creature with mana value 3 or less enters")
    void drawsForCreatureWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw when a creature with mana value greater than 3 enters")
    void doesNotDrawForCreatureWithManaValueGreaterThanThree() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(1);
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonqualifyingCreatureDoesNotConsumeTheTrigger() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(1);
        harness.setHand(player1, List.of(new HillGiant(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(1);
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SurvivorOfKorlis(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void eachWelcomeTriggersIndependently() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(2);
        harness.castFromHand(player1, new SurvivorOfKorlis(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousTokensDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(2);
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerIsAvailableAgainOnNextTurn() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(4);
        harness.castFromHand(player1, new SurvivorOfKorlis(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SurvivorOfKorlis(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prototypeCreatureWithManaValueExactlyThreeTriggers() {
        harness.addToBattlefield(player1, new TocasiasWelcome());
        seedLibrary(1);
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
