package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WarElephantTest extends BaseCardTest {

    @Test
    @DisplayName("War Elephant can band with one non-banding attacker")
    void canBandWithNonBandingAttacker() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new WarElephant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        elephant.setSummoningSick(false);
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(harness.getGameData(), player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(elephant.getBandId()).isNotNull();
        assertThat(elephant.getBandId()).isEqualTo(bears.getBandId());
    }

    @Test
    @DisplayName("War Elephant tramples excess combat damage over a blocker")
    void tramplesExcessDamage() {
        harness.setLife(player2, 20);
        Permanent elephant = addCreatureReady(player1, new WarElephant());
        elephant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.assertOnBattlefield(player1, "War Elephant");
    }
}
