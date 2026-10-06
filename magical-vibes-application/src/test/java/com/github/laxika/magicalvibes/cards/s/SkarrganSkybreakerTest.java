package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkarrganSkybreaker.class, GhorClanSavage.class})
class SkarrganSkybreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkybreaker();

        assertThat(findPermanent(player1, "Skarrgan Skybreaker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3 sees damage dealt after casting but before resolution")
    void bloodthirstAppliesWhenDamageOccursBeforeResolution() {
        harness.castFromHand(player1, new SkarrganSkybreaker(), "{4}{R}{R}{G}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Skarrgan Skybreaker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castSkybreaker();

        assertThat(findPermanent(player1, "Skarrgan Skybreaker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrifices itself and deals damage equal to its power to a player")
    void sacrificesSelfAndDealsPowerDamageToPlayer() {
        Permanent skybreaker = addCreatureReady(player1, new SkarrganSkybreaker());
        skybreaker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Skarrgan Skybreaker");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals damage equal to its power to a target creature")
    void dealsPowerDamageToCreature() {
        addCreatureReady(player1, new SkarrganSkybreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhorClanSavage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghor-Clan Savage");
    }

    @Test
    @DisplayName("Damage to its controller does not enable bloodthirst")
    void controllerDamageDoesNotEnableBloodthirst() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castSkybreaker();

        assertThat(findPermanent(player1, "Skarrgan Skybreaker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Skybreaker can sacrifice itself and deal six damage after bloodthirst")
    void bloodthirstPowerIsUsedAfterSacrifice() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkybreaker();
        findPermanent(player1, "Skarrgan Skybreaker").tap();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Skarrgan Skybreaker");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Its controller is a legal damage target")
    void canDealDamageToItsController() {
        addCreatureReady(player1, new SkarrganSkybreaker());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Skarrgan Skybreaker");
    }

    @Test
    @DisplayName("A creature controlled by its controller is a legal damage target")
    void canDealDamageToOwnCreature() {
        addCreatureReady(player1, new SkarrganSkybreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhorClanSavage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghor-Clan Savage");
        harness.assertInGraveyard(player1, "Skarrgan Skybreaker");
    }

    private void castSkybreaker() {
        harness.castFromHand(player1, new SkarrganSkybreaker(), "{4}{R}{R}{G}");
        resolveAllTriggers();
    }
}
