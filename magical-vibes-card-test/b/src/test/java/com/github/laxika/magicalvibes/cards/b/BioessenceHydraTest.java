package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AjaniTheGreathearted;
import com.github.laxika.magicalvibes.cards.k.KayasGhostform;
import com.github.laxika.magicalvibes.cards.k.KioraBehemothBeckoner;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BioessenceHydra.class, AjaniTheGreathearted.class, KioraBehemothBeckoner.class,
        NarsetParterOfVeils.class, KayasGhostform.class})
class BioessenceHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each loyalty counter on your planeswalkers")
    void entersWithCountersForControlledPlaneswalkerLoyalty() {
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 4);
        addReadyPlaneswalker(player2, new KioraBehemothBeckoner(), 7);

        harness.castFromHand(player1, new BioessenceHydra(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Triggers when a planeswalker enters with loyalty counters")
    void triggersForPlaneswalkerEnteringWithLoyalty() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());

        harness.castFromHand(player1, new AjaniTheGreathearted(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Triggers when a loyalty ability adds loyalty counters")
    void triggersForLoyaltyAbilityAddingCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void entersWithoutCountersWhenNoPlaneswalkersAreControlled() {
        addReadyPlaneswalker(player2, new AjaniTheGreathearted(), 5);

        harness.castFromHand(player1, new BioessenceHydra(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sumsLoyaltyAcrossMultipleControlledPlaneswalkers() {
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 5);
        addReadyPlaneswalker(player1, new KioraBehemothBeckoner(), 7);

        harness.castFromHand(player1, new BioessenceHydra(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
    }

    @Test
    void ignoresLoyaltyCountersAddedToOpponentsPlaneswalker() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());
        addReadyPlaneswalker(player2, new AjaniTheGreathearted(), 5);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsAllLoyaltyCountersPlacedByOneAbility() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 5);
        addReadyPlaneswalker(player1, new KioraBehemothBeckoner(), 7);
        addReadyPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        addReadyPlaneswalker(player2, new AjaniTheGreathearted(), 5);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void removingLoyaltyCountersDoesNotTriggerHydra() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 5);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersWhenPlaneswalkerReturnsFromGraveyardWithLoyaltyCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());
        Permanent ajani = addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 2);
        harness.setHand(player1, List.of(new KayasGhostform()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, ajani.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private Permanent addReadyPlaneswalker(Player player, com.github.laxika.magicalvibes.model.Card card,
                                           int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return planeswalker;
    }

    private Permanent findHydra(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BioessenceHydra)
                .findFirst()
                .orElseThrow();
    }
}
