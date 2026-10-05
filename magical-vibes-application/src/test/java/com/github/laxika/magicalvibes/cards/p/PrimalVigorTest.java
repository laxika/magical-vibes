package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarosGoneNuts;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalVigor.class, BladeSplicer.class, Pentavus.class, MarosGoneNuts.class,
        GrizzlyBears.class, TimberlandGuide.class, Triskelion.class, ThrabenInspector.class, Skinrender.class})
class PrimalVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles tokens created by an opponent")
    void doublesOpponentsTokens() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    @DisplayName("Doubles +1/+1 counters put on an opponent's creature")
    void doublesOpponentsCreatureCounters() {
        harness.addToBattlefield(player1, new PrimalVigor());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Maro's Gone Nuts quadruples Primal Vigor's global counter replacement")
    void marosGoneNutsQuadruplesOpponentsCreatureCounters() {
        harness.addToBattlefield(player1, new MarosGoneNuts());
        harness.addToBattlefield(player1, new PrimalVigor());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void doublesControllersTokens() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    void doublesCountersOnControllersCreature() {
        harness.addToBattlefield(player1, new PrimalVigor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doublesCountersAsOpponentsCreatureEnters() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Triskelion(), "{6}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Triskelion").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
    }

    @Test
    void copiesControlledByDifferentPlayersQuadrupleTokens() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.addToBattlefield(player2, new PrimalVigor());
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(4);
    }

    @Test
    void copiesControlledByDifferentPlayersQuadrupleCounters() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.addToBattlefield(player2, new PrimalVigor());
        harness.castFromHand(player1, new Triskelion(), "{6}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Triskelion").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(12);
    }

    @Test
    void doublesOpponentsNoncreatureTokens() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ThrabenInspector(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).hasSize(2);
    }

    @Test
    void doesNotDoubleCounterRemovalCosts() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.castFromHand(player1, new Pentavus(), "{7}");
        harness.passBothPriorities();
        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(findPermanents(player1, "Pentavite")).hasSize(2);
    }

    @Test
    void doesNotDoubleMinusOneMinusOneCounters() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.castFromHand(player1, new Pentavus(), "{7}");
        harness.passBothPriorities();
        Permanent pentavus = findPermanent(player1, "Pentavus");
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, List.of(pentavus.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(pentavus.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
