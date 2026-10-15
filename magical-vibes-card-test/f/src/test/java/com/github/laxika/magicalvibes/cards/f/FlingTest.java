package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fling.class, GrizzlyBears.class, RagingGoblin.class, ChandraNalaar.class})
class FlingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fling sacrifices a creature and stores its power in xValue")
    void castingSacrificesCreatureAndStoresPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2); // Grizzly Bears has 2 power

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast Fling without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for Fling")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Fling deals damage equal to sacrificed creature's power to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18); // 2 damage from Grizzly Bears' power
    }

    @Test
    @DisplayName("Fling deals damage equal to sacrificed creature's power to target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Sacrificed creature is gone
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Target creature should be dead (2 damage to 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fling with 1-power creature deals 1 damage")
    void onePowerCreatureDealsOneDamage() {
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Spell fizzles if target is removed before resolution — sacrifice still happens")
    void spellFizzlesIfTargetRemoved() {
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        // Sacrifice already happened as part of cost
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Spell fizzles — no damage dealt
        harness.assertLife(player2, 20);
    }

    @Test
    void usesModifiedPowerImmediatelyBeforeSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setPowerModifier(3);
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void nonpositivePowerDealsNoDamage() {
        for (int modifier : List.of(-2, -3)) {
            Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            sacrifice.setPowerModifier(modifier);
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new Fling()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 20);
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }
    }

    @Test
    void canTargetItsController() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void canTargetCreatureSacrificedForItsCostButDoesNotResolve() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fling");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsDamageToPlaneswalker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
    }
}
