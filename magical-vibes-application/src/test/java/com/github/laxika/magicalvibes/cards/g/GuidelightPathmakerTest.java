package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
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

class GuidelightPathmakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers all artifact cards")
    void acceptingEtbAbilityOffersArtifacts() {
        setupAndCast();
        OriginSpellbomb lowManaValue = new OriginSpellbomb();
        SolemnSimulacrum highManaValue = new SolemnSimulacrum();
        setLibrary(lowManaValue, highManaValue);

        resolveMayAbility(true);

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(lowManaValue, highManaValue);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("An artifact with mana value 2 or less enters the battlefield")
    void lowManaValueArtifactEntersBattlefield() {
        setupAndCast();
        OriginSpellbomb lowManaValue = new OriginSpellbomb();
        setLibrary(lowManaValue);
        resolveMayAbility(true);

        chooseCard(lowManaValue);

        harness.assertOnBattlefield(player1, "Origin Spellbomb");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).doesNotContain(lowManaValue);
    }

    @Test
    @DisplayName("An artifact with mana value greater than 2 goes to hand")
    void highManaValueArtifactGoesToHand() {
        setupAndCast();
        SolemnSimulacrum highManaValue = new SolemnSimulacrum();
        setLibrary(highManaValue);
        resolveMayAbility(true);

        chooseCard(highManaValue);

        harness.assertInHand(player1, "Solemn Simulacrum");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(highManaValue));
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast();
        setLibrary(new OriginSpellbomb());

        resolveMayAbility(false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new GuidelightPathmaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void resolveMayAbility(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void chooseCard(Card card) {
        GameData gameData = harness.getGameData();
        int index = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().indexOf(card);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }
}
