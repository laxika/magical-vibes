package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Exocrine.class, Forest.class, GrizzlyBears.class, GoForTheThroat.class})
class ExocrineTest extends BaseCardTest {

    @Test
    @DisplayName("At X=5, enters with counters, draws, and deals damage to players and other creatures")
    void ravenousAndBioPlasmicBarrageAtFive() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        gs.playCard(gd, player1, 0, 5, null, null);
        resolveAllTriggers();

        Permanent exocrine = findPermanent(player1, "Exocrine");
        assertThat(exocrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(exocrine.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("At X=4, ravenous does not draw")
    void ravenousDoesNotDrawBelowFive() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 4, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Exocrine")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("At X=0, Exocrine enters without counters and deals no damage or draws")
    void zeroXDoesNotDrawOrDealDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new Exocrine());
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Exocrine").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Exocrine").getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing counters in response does not stop the draw for X=5 or change barrage damage")
    void ravenousUsesPaidXAfterCountersAreRemoved() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0, 5);
        harness.passBothPriorities();
        findPermanent(player1, "Exocrine").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Adding counters in response does not grant a ravenous draw for X=4")
    void ravenousDoesNotDrawWhenCountersIncreasePastFive() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, 4);
        harness.passBothPriorities();
        findPermanent(player1, "Exocrine").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ravenous and Bio-plasmic Barrage trigger as two separate abilities at X=5")
    void drawAndBarrageAreSeparateTriggeredAbilities() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Exocrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Destroying Exocrine in response does not stop its draw or barrage")
    void abilitiesResolveAfterSourceIsDestroyed() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player2, new Exocrine());
        harness.setHand(player1, List.of(new Exocrine()));
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 5);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, findPermanent(player1, "Exocrine").getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Exocrine");
        harness.assertInGraveyard(player1, "Exocrine");
        harness.assertNotOnBattlefield(player2, "Exocrine");
        harness.assertInGraveyard(player2, "Exocrine");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }
}
