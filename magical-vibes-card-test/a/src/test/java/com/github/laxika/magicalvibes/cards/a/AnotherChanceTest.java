package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnotherChance.class, Forest.class, MineshaftSpider.class})
class AnotherChanceTest extends BaseCardTest {

    @Test
    void acceptingMayMillsThenReturnsUpToTwoCreatures() {
        harness.setGraveyard(player1, List.of(new MineshaftSpider(), new MineshaftSpider(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mineshaft Spider", "Mineshaft Spider");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest", "Forest", "Another Chance");
    }

    @Test
    void decliningMayStillReturnsUpToTwoCreatures() {
        harness.setGraveyard(player1, List.of(new MineshaftSpider(), new MineshaftSpider(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mineshaft Spider", "Mineshaft Spider");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Another Chance");
    }

    @Test
    void returnsOnlyTwoMatchingCreatureCards() {
        harness.setGraveyard(player1, List.of(
                new MineshaftSpider(), new MineshaftSpider(), new MineshaftSpider(), new Forest()));
        harness.setLibrary(player1, List.of());
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Mineshaft Spider", "Mineshaft Spider");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mineshaft Spider", "Forest", "Another Chance");
    }

    @Test
    void canReturnBothNewlyMilledCreatures() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new MineshaftSpider(), new MineshaftSpider(), new Forest()));
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mineshaft Spider", "Mineshaft Spider");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Another Chance");
    }

    @Test
    void canReturnZeroCreaturesAfterMilling() {
        harness.setGraveyard(player1, List.of(new MineshaftSpider(), new MineshaftSpider()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mineshaft Spider", "Mineshaft Spider", "Forest", "Forest", "Another Chance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canStopAfterReturningOneCreature() {
        harness.setGraveyard(player1, List.of(new MineshaftSpider(), new MineshaftSpider()));
        harness.setLibrary(player1, List.of());
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mineshaft Spider");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mineshaft Spider", "Another Chance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsOnlyAvailableCardAndCanReturnIt() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new MineshaftSpider()));
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mineshaft Spider");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Another Chance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithoutReturningNoncreaturesOrOpponentsCreatures() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new MineshaftSpider()));
        harness.setLibrary(player1, List.of());
        castAnotherChance();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Another Chance");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mineshaft Spider");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAnotherChance() {
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
