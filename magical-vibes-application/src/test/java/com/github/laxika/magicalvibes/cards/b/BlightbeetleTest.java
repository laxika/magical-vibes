package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlightbeetleTest extends BaseCardTest {

    @Test
    void opponentsCreaturesCannotGetPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void controllerCreaturesCanGetPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new Blightbeetle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void otherCounterTypesRemainAllowed() {
        harness.addToBattlefield(player1, new Blightbeetle());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, angel.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void protectionFromGreenPreventsGreenSpellsFromTargetingIt() {
        Permanent blightbeetle = harness.addToBattlefieldAndReturn(player1, new Blightbeetle());
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blightbeetle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
