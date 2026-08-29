package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowthSpasmTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land tapped and creates an Eldrazi Spawn")
    void searchesForBasicLandAndCreatesSpawn() {
        setLibrary(List.of(new Forest(), new GrizzlyBears()));
        castGrowthSpasm();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.hasType(CardType.LAND)).isTrue());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("The created Eldrazi Spawn can be sacrificed for colorless mana")
    void spawnCanBeSacrificedForColorlessMana() {
        setLibrary(List.of(new Forest()));
        castGrowthSpasm();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    private void castGrowthSpasm() {
        harness.setHand(player1, List.of(new GrowthSpasm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
