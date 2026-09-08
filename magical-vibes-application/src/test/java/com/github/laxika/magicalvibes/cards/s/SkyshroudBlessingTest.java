package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jinx;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

class SkyshroudBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("All lands gain shroud and the caster draws a card")
    void allLandsGainShroudAndDrawsCard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        Permanent ownForest = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        Permanent opponentMountain = gqs.findPermanentById(gd, harness.getPermanentId(player2, "Mountain"));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SkyshroudBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownForest, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentMountain, Keyword.SHROUD)).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Shroud prevents targeting a land")
    void shroudPreventsTargetingALand() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent mountain = gqs.findPermanentById(gd, harness.getPermanentId(player2, "Mountain"));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SkyshroudBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Jinx()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, mountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent mountain = gqs.findPermanentById(gd, harness.getPermanentId(player2, "Mountain"));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SkyshroudBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mountain, Keyword.SHROUD)).isFalse();
    }
}
