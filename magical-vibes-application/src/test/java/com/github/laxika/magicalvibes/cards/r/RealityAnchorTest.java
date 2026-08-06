package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
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

class RealityAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses shadow and the controller draws a card")
    void removesShadowAndDraws() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.getGameData().playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isTrue();

        harness.castInstant(player1, 0, soldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isFalse();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Shadow comes back at end of turn")
    void shadowReturnsAtCleanup() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, soldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
