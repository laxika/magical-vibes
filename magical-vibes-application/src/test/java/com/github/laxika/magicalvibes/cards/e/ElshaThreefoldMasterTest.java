package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.n.NyxFleeceRam;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElshaThreefoldMaster.class, Shock.class, NyxFleeceRam.class})
class ElshaThreefoldMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Monk tokens equal to combat damage dealt to a player")
    void createsMonksEqualToCombatDamage() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Elsha and the created Monk have prowess")
    void elshaAndCreatedMonkHaveProwess() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        Permanent monk = findPermanent(player1, "Monk");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }

    @Test
    void boostedCombatDamageCreatesMultipleMonksWithoutRetroactiveProwess() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        elsha.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(findPermanents(player1, "Monk")).hasSize(2).allSatisfy(monk -> {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
            assertThat(monk.isTapped()).isFalse();
            assertThat(monk.isAttacking()).isFalse();
        });
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent monk = findPermanent(player1, "Monk");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
    }

    @Test
    void enchantmentCreatureSpellDoesNotTriggerProwess() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        harness.setHand(player1, List.of(new NyxFleeceRam()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nyx-Fleece Ram");
        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(1);
    }

    @Test
    void combatDamageTriggerResolvesAfterElshaDies() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setAttacking(true);
        resolveCombat();
        assertThat(countPermanents(player1, "Monk")).isZero();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, elsha.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Elsha, Threefold Master");
        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Monk")).isZero();
    }

    @Test
    void onlyTrampleDamageToPlayerCountsForTokens() {
        Permanent elsha = addCreatureReady(player1, new ElshaThreefoldMaster());
        elsha.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent blocker = addCreatureReady(player2, new NyxFleeceRam());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Nyx-Fleece Ram");
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
    }

    @Test
    void damageOnlyToABlockerCreatesNoMonks() {
        addCreatureReady(player1, new ElshaThreefoldMaster());
        Permanent blocker = addCreatureReady(player2, new NyxFleeceRam());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Monk")).isZero();
    }
}
