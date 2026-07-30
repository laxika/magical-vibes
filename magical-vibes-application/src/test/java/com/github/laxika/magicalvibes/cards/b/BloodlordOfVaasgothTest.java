package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuskborneSkymarcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodlordOfVaasgothTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void ownBloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void ownBloodthirstDoesNotApply() {
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 3 granted only to the controller, not to an opponent's damage")
    void ownBloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A cast Vampire creature spell gains bloodthirst 3 and enters with three counters")
    void grantsBloodthirstToVampireSpell() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent skymarcher = findPermanent(player1, "Duskborne Skymarcher");
        assertThat(skymarcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted bloodthirst does nothing when no opponent was dealt damage")
    void grantedBloodthirstInactiveWithoutDamage() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Duskborne Skymarcher")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Vampire creature spell does not gain bloodthirst")
    void doesNotGrantBloodthirstToNonVampire() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBloodlord() {
        harness.setHand(player1, List.of(new BloodlordOfVaasgoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
