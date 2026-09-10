package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UnstableFrontier;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaidingNightstalker.class, BearCub.class, Swamp.class, UnstableFrontier.class})
class RaidingNightstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Raiding Nightstalker cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        Permanent atkPerm = addCreatureReady(player1, new RaidingNightstalker());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Raiding Nightstalker can be blocked when defending player does not control a Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        Permanent atkPerm = addCreatureReady(player1, new RaidingNightstalker());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Raiding Nightstalker can be blocked when only the attacking player controls a Swamp")
    void canBeBlockedWhenOnlyAttackingPlayerControlsSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        Permanent atkPerm = addCreatureReady(player1, new RaidingNightstalker());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Raiding Nightstalker can be blocked when a printed Swamp has become an Island")
    void canBeBlockedWhenPrintedSwampNoLongerHasSwampType() {
        harness.addToBattlefield(player2, new UnstableFrontier());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent blockerPerm = addCreatureReady(player2, new BearCub());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 1, null, swamp.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "ISLAND");
        assertThat(gqs.effectiveBasicLandTypes(gd, swamp)).containsExactly(CardSubtype.ISLAND);

        Permanent atkPerm = addCreatureReady(player1, new RaidingNightstalker());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
