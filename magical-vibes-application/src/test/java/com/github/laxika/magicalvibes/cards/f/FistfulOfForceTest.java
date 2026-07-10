package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class FistfulOfForceTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FistfulOfForce()));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G}
    }

    // Caster wins the clash: their revealed Grizzly Bears (MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash grants +4/+4 and trample")
    void wonClashGrantsFullBoostAndTrample() {
        prepare();
        harness.addToBattlefield(player1, new GrizzlyBears());
        stackClashWinForCaster();

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Losing the clash grants only +2/+2 and no trample")
    void lostClashGrantsBaseBoostOnly() {
        prepare();
        harness.addToBattlefield(player1, new GrizzlyBears());
        stackClashLossForCaster();

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        prepare();
        harness.addToBattlefield(player1, new GrizzlyBears());
        stackClashWinForCaster();

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepare();
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is castable
        harness.addToBattlefield(player1, new Forest());

        UUID landId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
