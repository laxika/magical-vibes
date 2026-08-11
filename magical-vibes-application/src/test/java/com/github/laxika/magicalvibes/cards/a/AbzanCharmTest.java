package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbzanCharmTest extends BaseCardTest {

    private void addWBG() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setDeck(Player player, List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    @Nested
    @DisplayName("Mode 0: Exile target creature with power 3 or greater")
    class ExileMode {

        @Test
        @DisplayName("Exiles a creature with power 3")
        void exilesLargeCreature() {
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new AbzanCharm()));
            addWBG();

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Hill Giant"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @DisplayName("Cannot target a creature with power 2")
        void cannotTargetSmallCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AbzanCharm()));
            addWBG();

            assertThatThrownBy(() -> harness.castInstant(
                    player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Mode 1 draws two cards and loses 2 life")
    void drawsTwoCardsAndLosesLife() {
        setDeck(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AbzanCharm()));
        addWBG();

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Nested
    @DisplayName("Mode 2: Distribute two +1/+1 counters among one or two target creatures")
    class CounterMode {

        @Test
        @DisplayName("Puts one counter on each of two target creatures")
        void distributesCountersAmongTwoCreatures() {
            Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AbzanCharm()));
            addWBG();

            harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{2},
                    List.of(first.getId(), second.getId()));
            harness.passBothPriorities();

            assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("Puts both counters on one target creature when only one is chosen")
        void putsBothCountersOnOneCreature() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AbzanCharm()));
            addWBG();

            harness.castModalInstantWithModes(player1, 0, 1, 1, new int[]{2}, List.of(bear.getId()));
            harness.passBothPriorities();

            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new AbzanCharm()));
            addWBG();

            assertThatThrownBy(() -> harness.castModalInstantWithModes(
                    player1, 0, 1, 1, new int[]{2}, List.of(fountain.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
