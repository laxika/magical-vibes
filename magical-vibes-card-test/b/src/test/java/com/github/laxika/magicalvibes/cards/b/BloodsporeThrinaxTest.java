package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodsporeThrinax.class, GrizzlyBears.class, Humble.class})
class BloodsporeThrinaxTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring a creature gives Bloodspore Thrinax a counter and boosts the next creature")
    void devourCountersBoostNextCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodsporeThrinax();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent thrinax = findPermanent(player1, "Bloodspore Thrinax");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter grant uses Bloodspore Thrinax's current counter count")
    void counterGrantUsesCurrentCounterCount() {
        Permanent thrinax = harness.addToBattlefieldAndReturn(player1, new BloodsporeThrinax());
        thrinax.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Devouring no creatures leaves Bloodspore Thrinax and later creatures without counters")
    void devourNoneAddsNoCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBloodsporeThrinax();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent thrinax = findPermanent(player1, "Bloodspore Thrinax");
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBloodsporeThrinax() {
        harness.castFromHand(player1, new BloodsporeThrinax(), "{2}{G}{G}");
    }

    @Test
    @DisplayName("Devour sacrifices each chosen creature and adds one counter per creature")
    void devouringMultipleCreaturesAddsOneCounterEach() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBloodsporeThrinax();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        Permanent thrinax = findPermanent(player1, "Bloodspore Thrinax");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodspore Thrinax resolves without a devour choice when no creatures are available")
    void enteringEmptyBattlefieldAddsNoCounters() {
        castBloodsporeThrinax();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Bloodspore Thrinax")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two Bloodspore Thrinaxes grant the sum of their counters")
    void multipleThrinaxesAddTheirCounterGrants() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodsporeThrinax());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodsporeThrinax());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Bloodspore Thrinax that has lost all abilities grants no entry counters")
    void losingAbilitiesStopsCounterGrant() {
        Permanent thrinax = harness.addToBattlefieldAndReturn(player1, new BloodsporeThrinax());
        thrinax.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, thrinax.getId());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
