package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AwakenTheBearTest extends BaseCardTest {

    @Test
    @DisplayName("Awaken the Bear gives target creature +3/+3 and trample")
    void boostsAndGrantsTrample() {
        castAwakenTheBearOnBears();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Awaken the Bear effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        castAwakenTheBearOnBears();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Awaken the Bear cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AwakenTheBear()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Awaken the Bear fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AwakenTheBear()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Awaken the Bear");
    }

    private void castAwakenTheBearOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AwakenTheBear()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
