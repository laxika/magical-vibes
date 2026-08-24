package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToppleTheStatue.class, FountainOfYouth.class, GrizzlyBears.class})
class ToppleTheStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and destroys a target artifact, then draws a card")
    void tapsDestroysArtifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castAt(target.getId());

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Taps but does not destroy a nonartifact permanent, then draws a card")
    void tapsNonartifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        castAt(target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(target);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new ToppleTheStatue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
