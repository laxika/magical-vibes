package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurderousRedcap.class, GrizzlyBears.class, LightningBolt.class})
class MurderousRedcapTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage (equal to power) to target creature, killing a 2/2")
    void etbDealsPowerDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MurderousRedcap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell, then the ETB triggered ability.
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 2 damage (equal to power) to target player")
    void etbDealsPowerDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MurderousRedcap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, player2.getId());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Persist returns Murderous Redcap with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new MurderousRedcap());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Murderous Redcap"));
        resolveAllTriggers();

        Permanent redcap = findPermanent(player1, "Murderous Redcap");
        assertThat(redcap).isNotNull();
        assertThat(redcap.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(redcap.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Murderous Redcap when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent redcap = harness.addToBattlefieldAndReturn(player1, new MurderousRedcap());
        redcap.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, redcap.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Murderous Redcap");
        harness.assertInGraveyard(player1, "Murderous Redcap");
    }

    @Test
    @DisplayName("ETB uses the source's power when the ability resolves")
    void etbUsesPowerAtResolution() {
        harness.setHand(player1, List.of(new MurderousRedcap()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent redcap = findPermanent(player1, "Murderous Redcap");
        redcap.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB uses last known power when the source dies before resolution")
    void etbUsesLastKnownPower() {
        harness.setHand(player1, List.of(new MurderousRedcap(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent redcap = findPermanent(player1, "Murderous Redcap");
        redcap.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.castInstant(player1, 0, redcap.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Murderous Redcap");
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Persist triggers another ETB that deals one damage, and a second death stays dead")
    void persistDealsReducedDamageAndReturnsOnlyOnce() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new MurderousRedcap());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, original.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        Permanent returned = findPermanent(player1, "Murderous Redcap");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.castInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Murderous Redcap");
        harness.assertInGraveyard(player1, "Murderous Redcap");
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can target its own controller")
    void etbCanDamageItsController() {
        harness.setHand(player1, List.of(new MurderousRedcap()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB deals no damage when the source's last known power is zero")
    void zeroPowerDealsNoDamage() {
        harness.setHand(player1, List.of(new MurderousRedcap()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        findPermanent(player1, "Murderous Redcap")
                .setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Murderous Redcap");
        harness.assertNotOnBattlefield(player1, "Murderous Redcap");
        assertThat(gd.stack).isEmpty();
    }
}
