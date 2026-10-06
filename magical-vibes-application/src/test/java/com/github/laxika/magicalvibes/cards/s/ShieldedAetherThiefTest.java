package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldedAetherThief.class, GrizzlyBears.class, Forest.class, HighGround.class})
class ShieldedAetherThiefTest extends BaseCardTest {

    @Test
    void getsEnergyWhenItBlocks() {
        addCreatureReady(player1, new ShieldedAetherThief());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyAndTapsToDrawACard() {
        Permanent thief = addCreatureReady(player1, new ShieldedAetherThief());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.playerEnergyCounters.put(player1.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(thief.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(forest);
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        addCreatureReady(player1, new ShieldedAetherThief());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    @Test
    void blockingMultipleCreaturesGetsOnlyOneEnergyCounter() {
        addCreatureReady(player1, new ShieldedAetherThief());
        harness.addToBattlefield(player1, new HighGround());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void energyAndTapArePaidBeforeDrawingAndOnlyThreeEnergyIsSpent() {
        Permanent thief = addCreatureReady(player1, new ShieldedAetherThief());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.playerEnergyCounters.put(player1.getId(), 5);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(thief.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(forest);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ShieldedAetherThief());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent thief = addCreatureReady(player1, new ShieldedAetherThief());
        thief.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void canBeCastOnOpponentsTurnBeforeCombat() {
        ShieldedAetherThief thief = new ShieldedAetherThief();
        harness.setHand(player1, List.of(thief));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shielded Aether Thief");
        harness.assertNotInHand(player1, "Shielded Aether Thief");
    }
}
