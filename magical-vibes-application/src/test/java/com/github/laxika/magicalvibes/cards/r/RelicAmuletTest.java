package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicAmulet.class, Divination.class, FugitiveWizard.class, GrizzlyBears.class, LightningBolt.class})
class RelicAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a charge counter on Relic Amulet")
    void castingInstantAddsChargeCounter() {
        Permanent amulet = addReadyAmulet(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery puts a charge counter on Relic Amulet")
    void castingSorceryAddsChargeCounter() {
        Permanent amulet = addReadyAmulet(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Wizard spell puts a charge counter on Relic Amulet")
    void castingWizardAddsChargeCounter() {
        Permanent amulet = addReadyAmulet(player1);
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Wizard creature does not put a charge counter on Relic Amulet")
    void castingNonWizardCreatureDoesNotAddChargeCounter() {
        Permanent amulet = addReadyAmulet(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing all charge counters deals that much damage to a target creature")
    void removesCountersAndDealsThatMuchDamage() {
        Permanent amulet = addReadyAmulet(player1);
        amulet.setCounterCount(CounterType.CHARGE, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, amulet), null, bears.getId());

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Relic Amulet cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent amulet = addReadyAmulet(player1);
        Permanent otherAmulet = addReadyAmulet(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, amulet), null, otherAmulet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAmulet(Player player) {
        Permanent amulet = new Permanent(new RelicAmulet());
        amulet.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(amulet);
        return amulet;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
