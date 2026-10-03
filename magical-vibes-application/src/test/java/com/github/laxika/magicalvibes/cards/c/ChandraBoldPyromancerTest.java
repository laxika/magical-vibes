package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenSentry;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.t.TeferiHeroOfDominaria;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraBoldPyromancer.class, AvenSentry.class, TeferiHeroOfDominaria.class,
        ColdWaterSnapper.class, ShortSword.class})
class ChandraBoldPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 ability adds {R}{R} and deals 2 damage to target player")
    void plusOneAddsManaAndDealsDamage() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        int redManaBefore = harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redManaBefore + 2);
    }

    @Test
    @DisplayName("-3 ability deals 3 damage to target creature")
    void minusThreeDeals3DamageToCreature() {
        Permanent chandra = addReadyChandra(player1);
        Permanent sentry = harness.addToBattlefieldAndReturn(player2, new AvenSentry());

        harness.activateAbility(player1, 0, 1, null, sentry.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3
        // Sentry should be dead (3 damage to a 3/2)
        harness.assertNotOnBattlefield(player2, "Aven Sentry");
        harness.assertInGraveyard(player2, "Aven Sentry");
    }

    @Test
    @DisplayName("-3 ability deals 3 damage to target planeswalker")
    void minusThreeDeals3DamageToPlaneswalker() {
        Permanent chandra = addReadyChandra(player1);

        // Add an opponent planeswalker with 3 loyalty
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiHeroOfDominaria());
        teferi.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, teferi.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3
        // Teferi should be put into the graveyard (3 damage to 3 loyalty planeswalker)
        harness.assertNotOnBattlefield(player2, "Teferi, Hero of Dominaria");
    }

    @Test
    @DisplayName("-7 ability deals 10 damage to target player and their creatures and planeswalkers")
    void minusSevenDeals10DamageToAll() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new AvenSentry());
        harness.addToBattlefield(player2, new AvenSentry());

        // Add an opponent planeswalker with 5 loyalty
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiHeroOfDominaria());
        teferi.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Chandra should have 0 loyalty (7 - 7) and be in graveyard
        harness.assertNotOnBattlefield(player1, "Chandra, Bold Pyromancer");

        // Player 2 takes 10 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10); // 20 - 10

        // Both sentries should be dead
        harness.assertNotOnBattlefield(player2, "Aven Sentry");
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Aven Sentry"))
                .count()).isEqualTo(2);

        // Teferi should be put into the graveyard (10 damage to 5 loyalty)
        harness.assertNotOnBattlefield(player2, "Teferi, Hero of Dominaria");
    }

    @Test
    @DisplayName("-7 ability does not damage controller's own permanents")
    void minusSevenDoesNotDamageOwnPermanents() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player1, new AvenSentry());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Player 1's sentry should be unharmed
        harness.assertOnBattlefield(player1, "Aven Sentry");
    }

    @Test
    @DisplayName("Cannot activate -7 when loyalty is only 5")
    void cannotActivateMinusSevenWithInsufficientLoyalty() {
        addReadyChandra(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("+1 waits for stack resolution before producing mana or damage")
    void plusOneUsesTheStack() {
        Permanent chandra = addReadyChandra(player1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore + 2);
    }

    @Test
    @DisplayName("+1 can target its controller and still gives mana to that controller")
    void plusOneCanTargetController() {
        addReadyChandra(player1);
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(manaBefore + 2);
    }

    @Test
    @DisplayName("-3 removes exactly three loyalty from a surviving planeswalker")
    void minusThreeLeavesPlaneswalkerWithRemainingLoyalty() {
        addReadyChandra(player1);
        Permanent teferi = harness.addToBattlefieldAndReturn(player2, new TeferiHeroOfDominaria());
        teferi.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, teferi.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Teferi, Hero of Dominaria");
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a player")
    void minusThreeCannotTargetPlayer() {
        Permanent chandra = addReadyChandra(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-7 damages hexproof creatures but leaves noncreature artifacts alone")
    void minusSevenDoesNotTargetAffectedPermanents() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new ColdWaterSnapper());
        harness.addToBattlefield(player2, new ShortSword());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Chandra, Bold Pyromancer");
        harness.assertInGraveyard(player2, "Cold-Water Snapper");
        harness.assertNotOnBattlefield(player2, "Cold-Water Snapper");
        harness.assertOnBattlefield(player2, "Short Sword");
    }

    @Test
    @DisplayName("-7 can target its controller and damage that player's permanents")
    void minusSevenCanTargetController() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 8);
        harness.addToBattlefield(player1, new AvenSentry());
        harness.addToBattlefield(player2, new AvenSentry());

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Chandra, Bold Pyromancer");
        harness.assertInGraveyard(player1, "Aven Sentry");
        harness.assertOnBattlefield(player2, "Aven Sentry");
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraBoldPyromancer());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
