package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bioplasm.class, Forest.class, GiantSpider.class})
class BioplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and gets its power and toughness until end of turn")
    void exiledCreatureBoostsBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        GiantSpider topCard = new GiantSpider();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(8);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling a noncreature card does not boost Bioplasm")
    void exiledNoncreatureDoesNotBoostBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        harness.setLibrary(player1, List.of(new GiantSpider()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("An empty library does not boost Bioplasm")
    void emptyLibraryDoesNotBoostBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
    }
}
