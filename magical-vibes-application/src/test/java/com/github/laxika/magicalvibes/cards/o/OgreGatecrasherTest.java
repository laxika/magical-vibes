package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreGatecrasher.class, WallOfWood.class, GrizzlyBears.class})
class OgreGatecrasherTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it destroys target creature with defender")
    void entersAndDestroysCreatureWithDefender() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        castOgreGatecrasher(wall.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertInGraveyard(player2, "Wall of Wood");
        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
    }

    @Test
    @DisplayName("It cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OgreGatecrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("defender");
    }

    @Test
    @DisplayName("It enters without an ETB trigger when no creature has defender")
    void entersWithoutTargetWhenNoCreatureHasDefender() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OgreGatecrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ogre Gatecrasher");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void castOgreGatecrasher(UUID targetId) {
        harness.setHand(player1, List.of(new OgreGatecrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetId);
    }
}
