package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SouthernElephant;
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

@CardUsed({Blaze.class, ChandraHopesBeacon.class, ChandraNalaar.class, ForestBear.class, HowlingMine.class, InvasionOfRegatha.class, Mountain.class, Plains.class, SouthernElephant.class})
class BlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blaze targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Blaze targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = creature.getId();
        harness.castSorcery(player1, 0, 2, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new Blaze()));
        // No mana at all — need at least {R}
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay X with any color mana")
    void canPayXWithAnyColor() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Deals X damage to target planeswalker")
    void dealsXDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreatureDestroysIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = creature.getId();
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        // Forest Bear (2/2) should be destroyed by 2 damage
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Does not destroy creature with toughness greater than X")
    void doesNotDestroyCreatureWithHigherToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SouthernElephant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = creature.getId();
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        // Southern Elephant (3/4) should survive 3 damage
        harness.assertOnBattlefield(player2, "Southern Elephant");
    }

    @CardUsed({ChandraHopesBeacon.class})
    @Test
    @DisplayName("Deals X damage to target planeswalker")
    void dealsXDamageToChandraHopesBeacon() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @CardUsed({DisciplesOfTheInferno.class, InvasionOfRegatha.class})
    @Test
    @DisplayName("Deals X damage to target battle")
    void dealsXDamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, battle.getId());

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        Blaze blaze = new Blaze();
        harness.setHand(player1, List.of(blaze));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(blaze);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Blaze goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Blaze blaze = new Blaze();
        harness.setHand(player1, List.of(blaze));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blaze);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).isEmpty();
    }

    @CardUsed(HowlingMine.class)
    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetNonCreatureNonPlaneswalkerPermanent() {
        Permanent howlingMine = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, howlingMine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
