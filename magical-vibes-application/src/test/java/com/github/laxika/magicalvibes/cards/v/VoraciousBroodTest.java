package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.Traumatize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousBrood.class, GrizzlyBears.class, Island.class, Traumatize.class})
class VoraciousBroodTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when only the opponent has creature cards in their graveyard")
    void ignoresOpponentsGraveyardOnEntry() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent brood = harness.enterBattlefieldAndReturn(player1, new VoraciousBrood());

        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Milling several creature cards together creates one trigger that puts that many counters")
    void simultaneousCreatureCardsCreateOneTrigger() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new VoraciousBrood());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Island(),
                new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(4);
        assertThat(gd.stack).hasSize(1);
        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milling only lands does not trigger")
    void millingNoncreaturesDoesNotTrigger() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new VoraciousBrood());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per creature card in your graveyard")
    void entersWithCountersForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Island()));

        Permanent brood = harness.enterBattlefieldAndReturn(player1, new VoraciousBrood());

        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature card enters your graveyard")
    void getsCounterWhenYourCreatureCardEntersGraveyard() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new VoraciousBrood());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count noncreature or opponent cards put into graveyards")
    void onlyYourCreatureCardsTrigger() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new VoraciousBrood());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposingBears));
        harness.passBothPriorities();

        assertThat(brood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
