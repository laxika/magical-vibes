package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShadowbornDemon;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KaaliaZenithSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Offers at most one Angel, Demon, and Dragon from the top six")
    void offersOneOfEachCreatureSubtype() {
        Card angel = new BaneslayerAngel();
        Card secondAngel = new BaneslayerAngel();
        Card demon = new ShadowbornDemon();
        Card dragon = new ShivanDragon();
        resolveKaalia(angel, demon, dragon, secondAngel, new GrizzlyBears(), new Shock());

        assertThat(currentSearch().params().cards()).containsExactly(angel, secondAngel);

        choose(angel);
        assertThat(currentSearch().params().cards()).containsExactly(demon);

        choose(demon);
        assertThat(currentSearch().params().cards()).containsExactly(dragon);

        choose(dragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, demon, dragon);
    }

    @Test
    @DisplayName("Skips missing subtypes and puts the unchosen cards on the bottom randomly")
    void skipsMissingSubtypeAndFinishesWithoutReorderPrompt() {
        Card angel = new BaneslayerAngel();
        Card dragon = new ShivanDragon();
        resolveKaalia(angel, dragon, new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());

        choose(angel);
        assertThat(currentSearch().params().cards()).containsExactly(dragon);

        choose(dragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, dragon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void resolveKaalia(Card... topCards) {
        harness.setHand(player1, List.of(new KaaliaZenithSeeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(topCards));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch currentSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void choose(Card chosenCard) {
        PendingInteraction.LibrarySearch search = currentSearch();
        int index = 0;
        while (!search.params().cards().get(index).getId().equals(chosenCard.getId())) {
            index++;
        }
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
