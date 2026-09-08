package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighlandGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeeSting.class, ChandraHopesBeacon.class, DelugeOfTheDead.class, GrizzlyBears.class,
        HighlandGiant.class, InvasionOfInnistrad.class})
class BeeStingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bee Sting targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Bee Sting deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Bee Sting deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bee Sting does not destroy a creature with toughness greater than 2")
    void doesNotDestroyHigherToughnessCreature() {
        harness.addToBattlefield(player2, new HighlandGiant());
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Highland Giant");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Highland Giant");
    }

    @Test
    @DisplayName("Bee Sting deals 2 damage to a target planeswalker")
    void deals2DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bee Sting deals 2 damage to a target battle")
    void deals2DamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bee Sting goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new BeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bee Sting");
    }
}
