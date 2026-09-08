package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AjaniTheGreathearted;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BioessenceHydra.class, AjaniTheGreathearted.class, ChandraNalaar.class})
class BioessenceHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each loyalty counter on your planeswalkers")
    void entersWithCountersForControlledPlaneswalkerLoyalty() {
        addReadyPlaneswalker(player1, new AjaniTheGreathearted(), 4);
        addReadyPlaneswalker(player2, new ChandraNalaar(), 6);

        harness.castFromHand(player1, new BioessenceHydra(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Triggers when a planeswalker enters with loyalty counters")
    void triggersForPlaneswalkerEnteringWithLoyalty() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new BioessenceHydra());

        harness.castFromHand(player1, new AjaniTheGreathearted(), "{3}{G}{W}");
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

    private Permanent addReadyPlaneswalker(Player player, com.github.laxika.magicalvibes.model.Card card,
                                           int loyalty) {
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
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
