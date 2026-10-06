package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraAblaze;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShardVolley.class, MurmuringBosk.class, ElvishWarrior.class, ChandraAblaze.class})
class ShardVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a land and puts spell on the stack")
    void castSacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Murmuring Bosk");
        harness.assertInGraveyard(player1, "Murmuring Bosk");
    }

    @Test
    @DisplayName("Resolving deals 3 damage to any target player")
    void resolvingDeals3DamageToPlayer() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Resolving deals 3 damage to a target creature")
    void resolvingDeals3DamageToCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, creature.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Resolving deals 3 damage to a target planeswalker")
    void resolvingDeals3DamageToPlaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, planeswalker.getId(), land.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast without a land to sacrifice")
    void cannotCastWithoutLand() {
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-land permanent")
    void cannotSacrificeNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice a tapped land and target yourself")
    void canSacrificeTappedLandAndTargetYourself() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        land.tap();
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), land.getId());
        harness.assertInGraveyard(player1, "Murmuring Bosk");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shard Volley");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land")
    void cannotSacrificeOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MurmuringBosk());
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Murmuring Bosk");
        harness.assertInHand(player1, "Shard Volley");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land that is not a creature")
    void cannotTargetNoncreatureLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MurmuringBosk());
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Murmuring Bosk");
        harness.assertOnBattlefield(player2, "Murmuring Bosk");
        harness.assertInHand(player1, "Shard Volley");
        assertThat(gd.stack).isEmpty();
    }
}
