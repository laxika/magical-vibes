package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MausoleumSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a revealed black card up to the creature count in the graveyard")
    void searchesForBlackCardWithinCreatureCount() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));
        Card disfigure = new Disfigure();
        Card doomBlade = new DoomBlade();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(disfigure, doomBlade, new RagingGoblin(), new Shock()));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(disfigure, doomBlade);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(
                search.params().cards().indexOf(doomBlade)));

        assertThat(gd.playerHands.get(player1.getId())).contains(doomBlade);
    }

    @Test
    @DisplayName("Excludes black cards above the creature count and nonblack cards")
    void appliesCreatureCountAndBlackFilters() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        Card disfigure = new Disfigure();
        Card doomBlade = new DoomBlade();
        Card goblin = new RagingGoblin();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(disfigure, doomBlade, goblin));

        castSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(disfigure);
    }

    @Test
    @DisplayName("Does not search when the graveyard has no creature cards")
    void noEligibleCardsWithNoCreatureCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Disfigure());

        castSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new MausoleumSecrets()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
