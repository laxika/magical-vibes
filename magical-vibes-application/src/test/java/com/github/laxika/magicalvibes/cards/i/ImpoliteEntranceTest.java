package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

class ImpoliteEntranceTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample and haste to the target creature, then draws a card")
    void grantsKeywordsAndDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImpoliteEntrance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trample and haste wear off at cleanup")
    void keywordsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImpoliteEntrance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ImpoliteEntrance()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
