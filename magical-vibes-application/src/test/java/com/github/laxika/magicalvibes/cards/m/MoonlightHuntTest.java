package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

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
                .hasMessageContaining("not playable");
    }
}
