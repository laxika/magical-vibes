package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.c.CaptainMarvelEarthsProtector;
import com.github.laxika.magicalvibes.cards.t.TheWondrousWasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkGammaGoliath.class, CaptainMarvelEarthsProtector.class, AmaranthineWall.class, TheWondrousWasp.class})
class HulkGammaGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Entry-turn Power-up puts five +1/+1 counters on Hulk")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent hulk = harness.enterBattlefieldAndReturn(player1, new HulkGammaGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Reduces another creature's Power-up ability by three generic mana")
    void reducesOtherControlledCreaturePowerUp() {
        addCreatureReady(player1, new HulkGammaGoliath());
        Permanent captainMarvel = addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce Hulk's own or non-Power-up abilities")
    void doesNotReduceOwnOrNonPowerUpAbilities() {
        addCreatureReady(player1, new HulkGammaGoliath());
        addCreatureReady(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Hulk pays his full Power-up cost after the entry turn")
    void ownPowerUpRequiresFullCostAfterEntryTurn() {
        Permanent hulk = addCreatureReady(player1, new HulkGammaGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power-up cannot be activated again even before its first activation resolves")
    void powerUpCanOnlyBeActivatedOnce() {
        Permanent hulk = harness.enterBattlefieldAndReturn(player1, new HulkGammaGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Hulk's reduction combines with the entry-turn discount and can make Power-up free")
    void combinesWithEntryTurnDiscount() {
        addCreatureReady(player1, new HulkGammaGoliath());
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captainMarvel.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opposing Hulk does not reduce your creature's Power-up cost")
    void doesNotReduceOpposingCreaturePowerUp() {
        addCreatureReady(player2, new HulkGammaGoliath());
        addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Hulk stops reducing Power-up costs when The Wondrous Wasp removes his abilities")
    void abilityRemovalDisablesCostReduction() {
        Permanent hulk = addCreatureReady(player1, new HulkGammaGoliath());
        addCreatureReady(player1, new CaptainMarvelEarthsProtector());
        harness.setHand(player1, List.of(new TheWondrousWasp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0, hulk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, hulk)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
