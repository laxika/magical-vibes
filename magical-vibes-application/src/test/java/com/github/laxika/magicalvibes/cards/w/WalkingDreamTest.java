package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingDream.class, SpinedWurm.class, EnsnaringBridge.class})
class WalkingDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Walking Dream cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new SpinedWurm());
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        dream.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Walking Dream untaps when each opponent controls fewer than two creatures")
    void untapsBelowOpponentCreatureThreshold() {
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        addCreatureReady(player2, new SpinedWurm());
        dream.tap();

        advanceToUpkeep(player1);

        assertThat(dream.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Walking Dream stays tapped when an opponent controls two creatures")
    void staysTappedAtOpponentCreatureThreshold() {
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        addCreatureReady(player2, new SpinedWurm());
        addCreatureReady(player2, new SpinedWurm());
        dream.tap();

        advanceToUpkeep(player1);

        assertThat(dream.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Walking Dream counts only creatures for its untap condition")
    void ignoresNoncreaturePermanents() {
        Permanent dream = addCreatureReady(player1, new WalkingDream());
        addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefieldAndReturn(player2, new EnsnaringBridge());
        dream.tap();

        advanceToUpkeep(player1);

        assertThat(dream.isTapped()).isFalse();
    }
}
