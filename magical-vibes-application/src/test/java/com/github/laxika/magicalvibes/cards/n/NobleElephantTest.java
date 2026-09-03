package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FemerefKnight;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NobleElephant.class, FemerefKnight.class, MtendaHerder.class})
class NobleElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Noble Elephant can band with one non-banding attacker")
    void canBandWithNonBandingAttacker() {
        Permanent elephant = addCreatureReady(player1, new NobleElephant());
        Permanent knight = addCreatureReady(player1, new FemerefKnight());

        declareBand(List.of(0, 1), List.of(List.of(0, 1)));

        assertThat(elephant.getBandId()).isNotNull();
        assertThat(elephant.getBandId()).isEqualTo(knight.getBandId());
    }

    @Test
    @DisplayName("Noble Elephant tramples excess combat damage over a blocker")
    void tramplesExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent elephant = addCreatureReady(player1, new NobleElephant());
        Permanent blocker = addCreatureReady(player2, new MtendaHerder());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elephant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    private void declareBand(List<Integer> attackerIndices, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackerIndices, null, bands));
    }
}
