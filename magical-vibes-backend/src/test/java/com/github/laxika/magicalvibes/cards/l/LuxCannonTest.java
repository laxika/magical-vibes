package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuxCannonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two activated abilities: charge counter and destroy")
    void hasTwoActivatedAbilities() {
        LuxCannon card = new LuxCannon();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: tap to put a charge counter
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);

        // Second ability: tap + remove 3 charge counters to destroy target permanent
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 3)
                .anyMatch(e -> e instanceof DestroyTargetPermanentEffect);
    }

    // ===== First ability: put a charge counter =====

    @Test
    @DisplayName("Tapping Lux Cannon puts a charge counter on it")
    void tapPutsChargeCounter() {
        harness.addToBattlefield(player1, new LuxCannon());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        assertThat(cannon.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can accumulate multiple charge counters over multiple turns")
    void accumulatesChargeCounters() {
        harness.addToBattlefield(player1, new LuxCannon());

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();

        // Activate three times (untapping between uses)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        cannon.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        cannon.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cannon.getChargeCounters()).isEqualTo(3);
    }

    // ===== Second ability: destroy target permanent =====

    @Test
    @DisplayName("Activating second ability with 3 charge counters destroys target permanent")
    void destroysTargetPermanent() {
        harness.addToBattlefield(player1, new LuxCannon());
        harness.addToBattlefield(player2, new LuxCannon()); // target

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        cannon.setChargeCounters(3);

        UUID targetId = harness.getPermanentId(player2, "Lux Cannon");
        int cannonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
        harness.activateAbility(player1, cannonIndex, 1, null, targetId);
        harness.passBothPriorities();

        // Charge counters are removed
        assertThat(cannon.getChargeCounters()).isEqualTo(0);

        // Target is destroyed
        harness.assertNotOnBattlefield(player2, "Lux Cannon");
        harness.assertInGraveyard(player2, "Lux Cannon");
    }

    @Test
    @DisplayName("Cannot activate second ability with fewer than 3 charge counters")
    void cannotDestroyWithFewerThanThreeCounters() {
        harness.addToBattlefield(player1, new LuxCannon());
        harness.addToBattlefield(player2, new LuxCannon());

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        cannon.setChargeCounters(2);

        UUID targetId = harness.getPermanentId(player2, "Lux Cannon");
        int cannonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
        assertThatThrownBy(() -> harness.activateAbility(player1, cannonIndex, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating with more than 3 charge counters only removes 3")
    void removesExactlyThreeCounters() {
        harness.addToBattlefield(player1, new LuxCannon());
        harness.addToBattlefield(player2, new LuxCannon());

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        cannon.setChargeCounters(5);

        UUID targetId = harness.getPermanentId(player2, "Lux Cannon");
        int cannonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
        harness.activateAbility(player1, cannonIndex, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(cannon.getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroy ability fizzles when target is removed before resolution")
    void destroyFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new LuxCannon());
        harness.addToBattlefield(player2, new LuxCannon());

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        cannon.setChargeCounters(3);

        UUID targetId = harness.getPermanentId(player2, "Lux Cannon");
        int cannonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
        harness.activateAbility(player1, cannonIndex, 1, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Counters are still removed (cost is paid on activation)
        assertThat(cannon.getChargeCounters()).isEqualTo(0);

        // Ability fizzles
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Tap constraint =====

    @Test
    @DisplayName("Cannot use both abilities in the same turn since both require tapping")
    void cannotUseBothAbilitiesSameTurn() {
        harness.addToBattlefield(player1, new LuxCannon());

        Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                .findFirst().orElseThrow();
        cannon.setChargeCounters(3);

        // Use first ability (tap to add counter)
        int cannonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
        harness.activateAbility(player1, cannonIndex, null, null);
        harness.passBothPriorities();

        // Now cannon is tapped — cannot activate second ability
        assertThat(cannon.isTapped()).isTrue();
        UUID targetId = harness.getPermanentId(player1, "Lux Cannon");
        assertThatThrownBy(() -> harness.activateAbility(player1, cannonIndex, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
