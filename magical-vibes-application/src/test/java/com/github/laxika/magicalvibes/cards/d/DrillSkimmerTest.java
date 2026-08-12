package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrillSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Has shroud while you control another artifact creature")
    void hasShroudWithAnotherArtifactCreature() {
        harness.addToBattlefield(player1, new DrillSkimmer());
        harness.addToBattlefield(player1, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Does not have shroud without another artifact creature")
    void hasNoShroudWithoutAnotherArtifactCreature() {
        harness.addToBattlefield(player1, new DrillSkimmer());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact creature does not satisfy the condition")
    void nonArtifactCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new DrillSkimmer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents both players from targeting it")
    void shroudPreventsTargeting() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DrillSkimmer());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0,
                harness.getPermanentId(player1, "Drill-Skimmer"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
