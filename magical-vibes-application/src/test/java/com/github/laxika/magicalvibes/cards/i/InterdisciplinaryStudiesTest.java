package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WortTheRaidmother;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterdisciplinaryStudies.class, EnvironmentalSciences.class, GrizzlyBears.class,
        WortTheRaidmother.class})
class InterdisciplinaryStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks a multicolored card, then learns")
    void seeksMulticoloredCardThenLearns() {
        Card multicolored = new WortTheRaidmother();
        Card monocolored = new GrizzlyBears();
        Card lesson = new EnvironmentalSciences();
        harness.setLibrary(player1, List.of(monocolored, multicolored));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new InterdisciplinaryStudies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(monocolored);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }
}
