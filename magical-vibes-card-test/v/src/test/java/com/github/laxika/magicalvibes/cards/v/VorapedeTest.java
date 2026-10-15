package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
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

@CardUsed({Vorapede.class, DoomBlade.class, GrizzlyBears.class, GrafdiggersCage.class})
class VorapedeTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance: Vorapede does not tap when attacking")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent vorapede = addCreatureReady(player1, new Vorapede());

        declareAttackers(player1, List.of(0));

        // Combat may auto-resolve and clear isAttacking; tapped state persists
        assertThat(vorapede.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent vorapede = addCreatureReady(player1, new Vorapede());
        vorapede.setAttacking(true);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Assign lethal damage to the blocker and the excess to the defending player.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Undying returns Vorapede with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, vorapede.getId());
        harness.passBothPriorities();

        Permanent returnedVorapede = findPermanent(player1, "Vorapede");
        assertThat(returnedVorapede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedVorapede.getEffectivePower()).isEqualTo(6);
        assertThat(returnedVorapede.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Vorapede");
    }

    @Test
    @DisplayName("Undying does not return Vorapede when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        vorapede.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, vorapede.getId());

        harness.assertNotOnBattlefield(player1, "Vorapede");
        harness.assertInGraveyard(player1, "Vorapede");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A returned Vorapede stays dead when destroyed again")
    void returnedVorapedeStaysDeadOnSecondDeath() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        harness.setHand(player2, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, vorapede.getId());
        harness.assertInGraveyard(player1, "Vorapede");
        harness.assertNotOnBattlefield(player1, "Vorapede");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vorapede");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Vorapede");
        harness.assertInGraveyard(player1, "Vorapede");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents the undying return")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, vorapede.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vorapede");
        harness.assertInGraveyard(player1, "Vorapede");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns a stolen Vorapede to its owner")
    void undyingReturnsToOwnerRatherThanController() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player2, new Vorapede());
        gd.stolenCreatures.put(vorapede.getId(), player1.getId());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, vorapede.getId());
        harness.assertInGraveyard(player1, "Vorapede");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vorapede");
        harness.assertNotOnBattlefield(player2, "Vorapede");
        harness.assertNotInGraveyard(player1, "Vorapede");
        assertThat(findPermanent(player1, "Vorapede")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
