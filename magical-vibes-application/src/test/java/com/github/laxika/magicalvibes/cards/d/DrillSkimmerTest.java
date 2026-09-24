package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrillSkimmer.class, DarksteelPendant.class, DroolingOgre.class, EchoingTruth.class})
class DrillSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Has shroud while you control another artifact creature")
    void hasShroudWithAnotherArtifactCreature() {
        addCreatureReady(player1, new DrillSkimmer());
        addCreatureReady(player1, new DrillSkimmer());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Does not have shroud without another artifact creature")
    void hasNoShroudWithoutAnotherArtifactCreature() {
        addCreatureReady(player1, new DrillSkimmer());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A non-artifact creature does not satisfy the condition")
    void nonArtifactCreatureDoesNotCount() {
        addCreatureReady(player1, new DrillSkimmer());
        addCreatureReady(player1, new DroolingOgre());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A noncreature artifact does not satisfy the condition")
    void nonCreatureArtifactDoesNotCount() {
        addCreatureReady(player1, new DrillSkimmer());
        harness.addToBattlefield(player1, new DarksteelPendant());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drill-Skimmer"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents an opponent from targeting it")
    void shroudPreventsOpponentTargeting() {
        addCreatureReady(player1, new DrillSkimmer());
        addCreatureReady(player1, new DrillSkimmer());
        harness.setHand(player2, List.of(new EchoingTruth()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Drill-Skimmer")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents its controller from targeting it")
    void shroudPreventsControllerTargeting() {
        addCreatureReady(player1, new DrillSkimmer());
        addCreatureReady(player1, new DrillSkimmer());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Drill-Skimmer")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
