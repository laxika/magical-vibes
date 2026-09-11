package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Earthcraft.class, Forest.class, LowlandGiant.class, Wasteland.class})
class EarthcraftTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target basic land by tapping a creature you control")
    void untapsTargetBasicLand() {
        addEarthcraft(player1);
        Permanent cost = addCreatureReady(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(cost.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a basic land an opponent controls")
    void untapsOpponentBasicLand() {
        addEarthcraft(player1);
        addCreatureReady(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonbasic land")
    void cannotTargetNonbasicLand() {
        addEarthcraft(player1);
        addCreatureReady(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Wasteland());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a basic land");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addEarthcraft(player1);
        addCreatureReady(player1, new LowlandGiant());
        Permanent creature = addCreatureReady(player2, new LowlandGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a basic land");
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        addEarthcraft(player1);
        Permanent creature = addCreatureReady(player1, new LowlandGiant());
        creature.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple untapped creatures, controller chooses which to tap")
    void multipleCreaturesChoice() {
        addEarthcraft(player1);
        Permanent bears1 = addCreatureReady(player1, new LowlandGiant());
        Permanent bears2 = addCreatureReady(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears1.getId());
        harness.passBothPriorities();

        assertThat(bears1.isTapped()).isTrue();
        assertThat(bears2.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can tap a creature with summoning sickness to pay the cost")
    void canTapCreatureWithSummoningSickness() {
        addEarthcraft(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay with a creature an opponent controls")
    void cannotPayWithOpponentsCreature() {
        addEarthcraft(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already-untapped basic land")
    void canTargetAlreadyUntappedBasicLand() {
        addEarthcraft(player1);
        Permanent creature = addCreatureReady(player1, new LowlandGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    private Permanent addEarthcraft(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Earthcraft());
    }
}
