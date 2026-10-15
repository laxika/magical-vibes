package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({LightningStrike.class, GrizzlyBears.class, HillGiant.class,
        ColossalDreadmaw.class, JaceCunningCastaway.class, Mountain.class})
class LightningStrikeTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Lightning Strike targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Lightning Strike");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Lightning Strike targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Lightning Strike");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast Lightning Strike without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Lightning Strike deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Lightning Strike deals 3 damage to target creature, destroying a 3/3")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Lightning Strike goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Strike");
    }

    @Test
    @DisplayName("Lightning Strike can target its controller")
    void deals3DamageToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lightning Strike marks 3 damage on a creature that survives")
    void survivingCreatureHas3DamageMarked() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Colossal Dreadmaw"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(findPermanent(player2, "Colossal Dreadmaw").getMarkedDamage()).isEqualTo(3);
        harness.assertNotInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Lightning Strike deals damage directly to a planeswalker")
    void deals3DamageToPlaneswalker() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway())
                .setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Jace, Cunning Castaway"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace, Cunning Castaway");
        harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lightning Strike cannot target a noncreature land")
    void cannotTargetNoncreatureLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Lightning Strike");
    }
}
