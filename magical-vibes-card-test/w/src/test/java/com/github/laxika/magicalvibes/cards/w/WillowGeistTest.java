package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WillowGeist.class, DiabolicEdict.class, Reminisce.class, Shock.class})
class WillowGeistTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenOneOrMoreCardsLeaveGraveyard() {
        Permanent geist = addReadyGeist();
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        addReminisceMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(geist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainsLifeEqualToItsPowerWhenItDies() {
        addReadyGeist();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void usesItsLastKnownPowerWhenItDies() {
        Permanent geist = addReadyGeist();
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotTriggerWhenOpponentsCardsLeaveTheirGraveyard() {
        Permanent geist = addReadyGeist();
        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        addReminisceMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(geist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnEmptyGraveyardIsShuffled() {
        Permanent geist = addReadyGeist();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        addReminisceMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(geist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersAgainForASeparateGraveyardDeparture() {
        Permanent geist = addReadyGeist();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce(), new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(geist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(geist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void deathTriggerGainsLifeForItsControllerOnly() {
        addCreatureReady(player2, new WillowGeist());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        harness.assertNotOnBattlefield(player2, "Willow Geist");
    }

    private Permanent addReadyGeist() {
        return addCreatureReady(player1, new WillowGeist());
    }

    private void addReminisceMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
