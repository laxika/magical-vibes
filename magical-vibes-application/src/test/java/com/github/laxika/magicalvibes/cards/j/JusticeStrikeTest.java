package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JusticeStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature when its power is lethal to itself")
    void destroysCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JusticeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Wall of Swords"));
        harness.passBothPriorities();

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
}
