package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LooterIlKor;
import com.github.laxika.magicalvibes.cards.t.TraitorsClutch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherflameWall.class, LooterIlKor.class, AshcoatBear.class, TraitorsClutch.class})
class AetherflameWallTest extends BaseCardTest {

    @Test
    @DisplayName("Aetherflame Wall can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        addCreatureReady(player1, new LooterIlKor());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Aetherflame Wall still blocks a creature without shadow")
    void blocksNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        addCreatureReady(player1, new AshcoatBear());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without Aetherflame Wall's ability cannot block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        addCreatureReady(player2, new AshcoatBear());
        addCreatureReady(player1, new LooterIlKor());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aetherflame Wall gets +1/+0 until end of turn")
    void firebreathingBoostsPower() {
        Permanent wall = addCreatureReady(player1, new AetherflameWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Aetherflame Wall's boost wears off at end of turn")
    void firebreathingBoostWearsOff() {
        Permanent wall = addCreatureReady(player1, new AetherflameWall());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
    }

    @Test
    @DisplayName("Aetherflame Wall with shadow cannot block a shadow attacker")
    void wallWithShadowCannotBlockShadowAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        addCreatureReady(player1, new LooterIlKor());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aetherflame Wall with shadow cannot block a nonshadow attacker")
    void wallWithShadowCannotBlockNormalAttacker() {
        Permanent wall = addCreatureReady(player2, new AetherflameWall());
        addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new TraitorsClutch()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated firebreathing activations work while tapped and summoning sick")
    void firebreathingStacksWithoutTapOrHasteRequirement() {
        Permanent wall = addCreatureReady(player1, new AetherflameWall());
        wall.setSummoningSick(true);
        wall.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(wall.isTapped()).isTrue();
    }
}
