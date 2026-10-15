package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.v.VastwoodGorger;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaRift.class, Mountain.class, LlanowarElves.class, VastwoodGorger.class})
class MagmaRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Deals exactly five damage without destroying a six-toughness creature")
    void dealsExactlyFiveDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VastwoodGorger());
        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Vastwood Gorger");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Magma Rift");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VastwoodGorger());
        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vastwood Gorger");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land even when controlling another land")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent opponentsLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VastwoodGorger());
        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), opponentsLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Magma Rift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting sacrifices a land and puts the sorcery on the stack")
    void castSacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to target creature")
    void resolvingDealsFiveDamageToCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a land to sacrifice")
    void cannotCastWithoutLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-land permanent")
    void cannotSacrificeNonLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.setHand(player1, List.of(new MagmaRift()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
