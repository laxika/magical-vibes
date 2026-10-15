package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HumanTorchJohnnyStorm;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarvelBoyNohVarr.class, GrizzlyBears.class, HumanTorchJohnnyStorm.class, AmaranthineWall.class, TurnToFrog.class})
class MarvelBoyNohVarrTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureEntersUnderYourControl() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenYouActivatePowerUpAbility() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addToBattlefield(player1, new HumanTorchJohnnyStorm());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOtherActivatedAbilities() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({MarvelBoyNohVarr.class})
    void doesNotTriggerForItsOwnEntry() {
        Permanent marvelBoy = harness.enterBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({MarvelBoyNohVarr.class})
    void doesNotTriggerWhenAnOpponentsCreatureEnters() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());

        Permanent opposingMarvelBoy = harness.enterBattlefieldAndReturn(player2, new MarvelBoyNohVarr());
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingMarvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({MarvelBoyNohVarr.class, GrizzlyBears.class})
    void triggersForEachCreatureEnteringBeforeTheTriggersResolve() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({MarvelBoyNohVarr.class, HumanTorchJohnnyStorm.class})
    void doesNotTriggerWhenAnOpponentActivatesPowerUp() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addToBattlefield(player2, new HumanTorchJohnnyStorm());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({MarvelBoyNohVarr.class, HumanTorchJohnnyStorm.class, TurnToFrog.class})
    void doesNotTriggerForPowerUpAfterLosingAllAbilities() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addToBattlefield(player1, new HumanTorchJohnnyStorm());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, marvelBoy.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
