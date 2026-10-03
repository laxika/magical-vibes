package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarruksWarsteed.class, GarrukSavageHerald.class, Forest.class})
class GarruksWarsteedTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCreatesOptionalSearchPrompt() {
        castWarsteed();

        resolveCreature();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void searchesLibraryForGarrukSavageHerald() {
        Card garruk = new GarrukSavageHerald();
        Card forest = new Forest();
        setLibrary(garruk, forest);
        castWarsteed();

        resolveMay(true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(garruk.getId());

        harness.handleMultipleCardsChosen(player1, List.of(garruk.getId()));

        harness.assertInHand(player1, "Garruk, Savage Herald");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void searchesGraveyardForGarrukSavageHerald() {
        Card garruk = new GarrukSavageHerald();
        harness.setGraveyard(player1, List.of(garruk));
        castWarsteed();

        resolveMay(true);

        harness.handleMultipleCardsChosen(player1, List.of(garruk.getId()));

        harness.assertInHand(player1, "Garruk, Savage Herald");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(garruk);
    }

    @Test
    void searchExcludesCardsWithOtherNames() {
        Card forest = new Forest();
        setLibrary(forest);
        castWarsteed();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    private void castWarsteed() {
        harness.setHand(player1, List.of(new GarruksWarsteed()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveCreature() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveMay(boolean choice) {
        resolveCreature();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
