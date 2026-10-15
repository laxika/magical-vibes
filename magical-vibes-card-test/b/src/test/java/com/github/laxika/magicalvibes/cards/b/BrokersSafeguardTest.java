package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BrokersSafeguard.class, GrizzlyBears.class, Memnite.class, Shock.class})
class BrokersSafeguardTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a nonartifact creature and it enters with a shield counter")
    void flickersCreatureWithShieldCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBrokersSafeguard();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("The perpetual entry ability applies again on a later flicker")
    void perpetualEntryAbilityAccumulates() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrokersSafeguard(), new BrokersSafeguard()));
        addManaForTwoSpells();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.SHIELD)).isEqualTo(2);
    }

    @Test
    @DisplayName("The shield counter prevents the next damage event")
    void shieldCounterPreventsDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBrokersSafeguard();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        assertThat(returned.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.setHand(player1, List.of(new BrokersSafeguard()));
        addManaForOneSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonartifact creature you control");
    }

    private void castBrokersSafeguard() {
        harness.setHand(player1, List.of(new BrokersSafeguard()));
        addManaForOneSpell();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
    }

    private void addManaForOneSpell() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void addManaForTwoSpells() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
