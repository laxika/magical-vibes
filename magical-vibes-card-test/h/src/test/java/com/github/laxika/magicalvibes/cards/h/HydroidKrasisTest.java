package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.Absorb;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydroidKrasis.class, SauroformHybrid.class, Absorb.class})
class HydroidKrasisTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=5 gains 2 life, draws 2 cards, and enters with 5 counters")
    void castTriggerAndCountersUseXValue() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new SauroformHybrid(), new SauroformHybrid(), new SauroformHybrid()));
        harness.setHand(player1, List.of(new HydroidKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        Permanent krasis = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting with X=3 rounds each half down")
    void castTriggerRoundsHalfDown() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new SauroformHybrid(), new SauroformHybrid()));
        harness.setHand(player1, List.of(new HydroidKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        Permanent krasis = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    @DisplayName("Zero, one, and even X values use the chosen X for both abilities")
    void smallAndEvenXValues(int x) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new SauroformHybrid(), new SauroformHybrid(), new SauroformHybrid()));
        harness.setHand(player1, List.of(new HydroidKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }

        gs.playCard(gd, player1, 0, x, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20 + x / 2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(x / 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        if (x == 0) {
            harness.assertNotOnBattlefield(player1, "Hydroid Krasis");
            harness.assertInGraveyard(player1, "Hydroid Krasis");
        } else {
            Permanent krasis = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(x);
            harness.assertNotInGraveyard(player1, "Hydroid Krasis");
        }
        harness.assertLife(player1, 20 + x / 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(x / 2);
    }

    @Test
    @DisplayName("Countering the spell does not stop its cast trigger")
    void castTriggerResolvesAfterSpellIsCountered() {
        HydroidKrasis krasis = new HydroidKrasis();
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new SauroformHybrid(), new SauroformHybrid(), new SauroformHybrid()));
        harness.setHand(player1, List.of(krasis));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 5, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, krasis.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hydroid Krasis");
        harness.assertNotOnBattlefield(player1, "Hydroid Krasis");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Hydroid Krasis");
        assertThat(gd.stack).isEmpty();
    }
}
