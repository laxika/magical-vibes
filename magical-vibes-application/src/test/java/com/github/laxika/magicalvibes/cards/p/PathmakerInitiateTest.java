package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BruteStrength;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathmakerInitiate.class, ThoseWhoServe.class, Colossapede.class, BruteStrength.class, Forest.class})
class PathmakerInitiateTest extends BaseCardTest {

    private Permanent addInitiate() {
        return addCreatureReady(player1, new PathmakerInitiate());
    }

    @Test
    @DisplayName("Makes a power-2-or-less creature unblockable this turn, then it wears off")
    void makesCreatureUnblockable() {
        addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        UUID targetId = harness.getPermanentId(player1, "Those Who Serve");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(target.isCantBeBlocked()).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Tap cost taps Pathmaker Initiate")
    void tapCostTapsInitiate() {
        Permanent initiate = addInitiate();
        harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        UUID targetId = harness.getPermanentId(player1, "Those Who Serve");
        harness.activateAbility(player1, 0, 0, null, targetId);

        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addInitiate();
        harness.addToBattlefield(player1, new Colossapede()); // 5/5

        UUID targetId = harness.getPermanentId(player1, "Colossapede");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItself() {
        Permanent initiate = addInitiate();

        harness.activateAbility(player1, 0, 0, null, initiate.getId());
        harness.passBothPriorities();

        assertThat(initiate.isCantBeBlocked()).isTrue();
    }

    @Test
    void canTargetOpponentsCreature() {
        addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new PathmakerInitiate());
        initiate.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, initiate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(initiate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent initiate = addInitiate();
        initiate.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, initiate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreature() {
        Permanent initiate = addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(initiate.isTapped()).isFalse();
    }

    @Test
    void targetBecomingTooPowerfulBeforeResolutionIsIllegal() {
        Permanent initiate = addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(initiate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerIncreaseAfterResolutionDoesNotRemoveUnblockability() {
        addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new BruteStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void affectedCreatureCannotBeBlockedInCombat() {
        addInitiate();
        Permanent target = addCreatureReady(player1, new ThoseWhoServe());
        addCreatureReady(player2, new Colossapede());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
