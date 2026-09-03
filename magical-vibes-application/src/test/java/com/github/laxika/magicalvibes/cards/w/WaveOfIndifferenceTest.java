package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaveOfIndifference.class, GrizzlyBears.class, Mountain.class})
class WaveOfIndifferenceTest extends BaseCardTest {

    @Test
    @DisplayName("X target creatures can't block this turn")
    void targetCreaturesCannotBlockThisTurn() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent firstTarget = addReadyCreature(player2, new GrizzlyBears());
        Permanent secondTarget = addReadyCreature(player2, new GrizzlyBears());
        Permanent untargeted = addReadyCreature(player2, new GrizzlyBears());

        castWave(2, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(firstTarget.isCantBlockThisTurn()).isTrue();
        assertThat(secondTarget.isCantBlockThisTurn()).isTrue();
        assertThat(untargeted.isCantBlockThisTurn()).isFalse();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X must equal the number of creature targets")
    void requiresExactlyXTargets() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The blocking restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        castWave(1, List.of(target.getId()));
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    private void castWave(int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WaveOfIndifference()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
