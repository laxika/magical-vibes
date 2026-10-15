package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FireServant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealersHawk;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JusticeStrike.class, GrizzlyBears.class, Plains.class, WallOfSwords.class,
        FireServant.class, HealersHawk.class})
class JusticeStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature when its power is lethal to itself")
    void destroysCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marks damage equal to the target creature's power")
    void dealsDamageEqualToPower() {
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wall of Swords"));

        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, plainsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fire Servant does not double damage dealt by the target creature")
    void spellDamageMultiplierDoesNotApplyToCreatureDamage() {
        harness.addToBattlefield(player1, new FireServant());
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wall of Swords"));

        harness.assertOnBattlefield(player2, "Wall of Swords");
        assertThat(findPermanent(player2, "Wall of Swords").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature with lifelink gains life for its controller before dying")
    void targetLifelinkGainsLifeForTargetController() {
        harness.addToBattlefield(player2, new HealersHawk());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Healer's Hawk"));

        harness.assertInGraveyard(player2, "Healer's Hawk");
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -4})
    @DisplayName("A creature with zero or negative power deals no damage")
    void nonpositivePowerDealsNoDamage(int powerModifier) {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        wall.setPowerModifier(powerModifier);
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, wall.getId());

        harness.assertOnBattlefield(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target your own creature and uses its power at resolution")
    void usesCurrentPowerOfOwnCreature() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfSwords());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, wall.getId());
        wall.setPowerModifier(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(4);
    }
}
