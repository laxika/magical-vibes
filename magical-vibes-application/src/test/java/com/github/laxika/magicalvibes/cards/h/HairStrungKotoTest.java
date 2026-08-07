package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HairStrungKotoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped creature makes target player mill a card")
    void tapCreatureMillsTargetPlayer() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller can be chosen as the milling player")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick creature can still pay the cost (no tap symbol)")
    void summoningSickCreaturePaysCost() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
