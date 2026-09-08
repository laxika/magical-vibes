package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MisstepTest extends BaseCardTest {

    @Test
    @DisplayName("Locks all creatures the target player controls through their next untap step")
    void locksTargetPlayersCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        targetCreature.tap();

        castMisstep(player2.getId());

        assertThat(targetCreature.getSkipUntapCount()).isEqualTo(1);

        advanceToNextTurn(player1);
        assertThat(targetCreature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not affect non-creatures or creatures controlled by another player")
    void affectsOnlyTargetPlayersCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent targetArtifact = gd.playerBattlefields.get(player2.getId()).getFirst();
        ownCreature.tap();
        targetArtifact.tap();

        castMisstep(player2.getId());

        assertThat(ownCreature.getSkipUntapCount()).isZero();
        assertThat(targetArtifact.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetSelf() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.tap();

        castMisstep(player1.getId());

        assertThat(ownCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an object that is not a player")
    void requiresPlayerTarget() {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMisstep(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Misstep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
