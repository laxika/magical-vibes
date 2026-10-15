package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BredForTheHunt.class, GrizzlyBears.class, Plains.class, KraulWarrior.class})
class BredForTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Creature with a +1/+1 counter deals combat damage — controller may draw a card")
    void counteredCreatureDamageDrawsCard() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the trigger draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Creature without a +1/+1 counter does not trigger")
    void noCounterDoesNotTrigger() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains()));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void eachCounteredCreatureOffersOneDrawRegardlessOfCounterCount() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KraulWarrior(), new KraulWarrior()));
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new KraulWarrior());
            attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            attacker.setAttacking(true);
        }

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingCounterAfterDamageDoesNotCancelDraw() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KraulWarrior()));
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kraul Warrior");
    }

    @Test
    void opposingCounteredCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KraulWarrior()));
        Permanent attacker = addCreatureReady(player2, new KraulWarrior());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void otherCounterTypeDoesNotTrigger() {
        harness.addToBattlefield(player1, new BredForTheHunt());
        harness.setHand(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
