package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.g.GrizzledAngler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonlightHunt.class, YoungWolf.class, GreaterWerewolf.class, GrizzlyBears.class,
        LlanowarElves.class, AirElemental.class, GrizzledAngler.class})
class MoonlightHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Wolves and Werewolves deal their power as damage, killing the target")
    void packDamageKillsTarget() {
        // Young Wolf 1/1 + Greater Werewolf 2/4 = 3 damage vs Llanowar Elves 1/1
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Non-Wolf creatures do not contribute damage")
    void nonWolfDoesNotContribute() {
        // Young Wolf 1 + Grizzly Bears 2 (ignored) = 1 damage vs Air Elemental 4/4
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(elemental.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("No Wolves or Werewolves deals no damage")
    void noPackDealsNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, elves.getId());

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(elves.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you don't control");
    }

    @Test
    @DisplayName("Tapped Wolves deal damage without taking damage back; opposing Wolves do not contribute")
    void tappedWolfDealsDamageWithoutFighting() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        wolf.tap();
        harness.addToBattlefield(player2, new YoungWolf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzledAngler());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(wolf.getMarkedDamage()).isZero();
        assertThat(wolf.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Young Wolf");
    }

    @Test
    @DisplayName("Wolves entering before resolution contribute damage")
    void packIsDeterminedAtResolution() {
        harness.addToBattlefield(player1, new YoungWolf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzledAngler());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new YoungWolf());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzled Angler");
    }

    @Test
    @DisplayName("Damage uses the Wolf's current power at resolution")
    void usesPowerAtResolution() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzledAngler());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(wolf.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Both Wolves and Werewolves contribute their individual power")
    void bothPackSubtypesContributeDamage() {
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoonlightHunt()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }
}
