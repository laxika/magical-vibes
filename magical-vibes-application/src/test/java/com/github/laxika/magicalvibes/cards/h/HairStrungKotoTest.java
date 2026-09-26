package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HairStrungKoto.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Only a creature controlled by the activator can pay the cost")
    void opponentCreatureCannotPayCost() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller chooses which untapped creature pays the cost")
    void choosesCreatureToTapWhenMultipleAreAvailable() {
        Permanent koto = harness.addToBattlefieldAndReturn(player1, new HairStrungKoto());
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosenBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(koto),
                null, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosenBears.getId());
        harness.passBothPriorities();

        assertThat(firstBears.isTapped()).isFalse();
        assertThat(chosenBears.isTapped()).isTrue();
        assertThat(koto.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
