package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DalekDrone.class, GrizzlyBears.class, PlatinumEmperion.class})
class DalekDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying an opponent's creature and making that player lose 3 life")
    void destroysOpponentCreatureAndMakesItsControllerLoseLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DalekDrone()));
        addMana();
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dalek Drone");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DalekDrone()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys Platinum Emperion before its controller loses life")
    void destroysLifeLockBeforeApplyingLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new DalekDrone()));
        addMana();
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Platinum Emperion");
        harness.assertOnBattlefield(player1, "Dalek Drone");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Enters without life loss when no opposing creature can be targeted")
    void entersWithoutAnOpposingCreature() {
        harness.castFromHand(player1, new DalekDrone(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dalek Drone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
