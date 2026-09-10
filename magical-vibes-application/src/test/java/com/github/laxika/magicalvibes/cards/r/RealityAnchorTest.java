package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityAnchor.class, SoltariFootSoldier.class, LotusPetal.class, LowlandGiant.class})
class RealityAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature loses shadow and the controller draws a card")
    void removesShadowAndDraws() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.getGameData().playerDecks.get(player1.getId()).add(new SoltariFootSoldier());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isTrue();

        harness.castAndResolveInstant(player1, 0, soldier.getId());

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isFalse();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Shadow comes back at end of turn")
    void shadowReturnsAtCleanup() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, soldier.getId());

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Still draws when the target creature does not have shadow")
    void drawsWhenTargetLacksShadow() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandGiant());
        harness.setLibrary(player1, List.of(new SoltariFootSoldier()));
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gqs.hasKeyword(gd, giant, Keyword.SHADOW)).isFalse();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent lotusPetal = harness.addToBattlefieldAndReturn(player2, new LotusPetal());
        harness.setHand(player1, List.of(new RealityAnchor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lotusPetal.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
