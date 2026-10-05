package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirelurkQueen.class, PersistentPetitioners.class, GrizzlyBears.class, Forest.class, RestInPeace.class})
class MirelurkQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gives two rad counters to the chosen player")
    void entersGivesTwoRadCountersToTargetPlayer() {
        harness.enterBattlefieldAndReturn(player1, new MirelurkQueen());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Milling a nonland card draws a card and puts a counter on Mirelurk Queen")
    void nonlandMillingDrawsAndPutsCounterOnQueen() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("The mill trigger resolves only once each turn")
    void millTriggerOnlyResolvesOnceEachTurn() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent firstPetitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        Permanent secondPetitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        firstPetitioners.setSummoningSick(false);
        secondPetitioners.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }
    @Test
    void entersCanGiveRadCountersToItsController() {
        harness.enterBattlefieldAndReturn(player1, new MirelurkQueen());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isNull();
    }

    @Test
    void landOnlyMillDoesNotConsumeTheTriggerForTheTurn() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new Forest(), new MirelurkQueen()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 2, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void millingControllersLibraryAlsoTriggers() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        Card milled = new MirelurkQueen();
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(milled, drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void drawStillResolvesAfterQueenLeavesAndDoesNotCounterANewQueen() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new MirelurkQueen()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, queen));
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonlandMilledIntoExileStillTriggers() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        Permanent petitioners = harness.addToBattlefieldAndReturn(player1, new PersistentPetitioners());
        petitioners.setSummoningSick(false);
        harness.addToBattlefield(player1, new RestInPeace());
        Card milled = new MirelurkQueen();
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(milled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Mirelurk Queen");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void severalNonlandsMilledTogetherGiveOnlyOneDrawAndCounter() {
        Permanent queen = harness.addToBattlefieldAndReturn(player1, new MirelurkQueen());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PersistentPetitioners());
        }
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.setLibrary(player2, List.of(new MirelurkQueen(), new Forest(), new MirelurkQueen()));

        harness.activateAbility(player1, 1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(queen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }
}
