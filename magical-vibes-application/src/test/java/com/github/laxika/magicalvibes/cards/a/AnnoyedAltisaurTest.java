package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnnoyedAltisaur.class, GrizzlyBears.class})
class AnnoyedAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade offers a lower-mana-value card and casts it for free")
    void cascadeCastsLowerManaValueCardForFree() {
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        GrizzlyBears hit = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(hit);
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }
}
