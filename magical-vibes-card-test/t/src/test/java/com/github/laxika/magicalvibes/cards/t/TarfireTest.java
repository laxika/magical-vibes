package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tarfire.class, WoodlandChangeling.class, ChandraNalaar.class, Mountain.class, OakgnarlWarrior.class})
class TarfireTest extends BaseCardTest {

    @Test
    @DisplayName("Tarfire deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tarfire deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Woodland Changeling");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Cannot cast Tarfire without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Tarfire()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tarfire goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tarfire");
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void dealsNonlethalDamageToOwnCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new OakgnarlWarrior());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        harness.assertOnBattlefield(player1, "Oakgnarl Warrior");
        assertThat(warrior.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void removesTwoLoyaltyFromPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetNoncreatureLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInHand(player1, "Tarfire");
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tarfire(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Woodland Changeling");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Tarfire).hasSize(2);
        harness.assertLife(player2, 20);
    }
}
