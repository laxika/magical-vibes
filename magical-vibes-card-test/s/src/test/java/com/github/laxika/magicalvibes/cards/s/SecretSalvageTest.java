package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecretSalvage.class, Ornithopter.class, WalkingBallista.class, SpireOfIndustry.class})
class SecretSalvageTest extends BaseCardTest {

    @Test
    void exilesTargetAndSearchesForAnyNumberOfSameNameCards() {
        Card target = new Ornithopter();
        Card firstCopy = new Ornithopter();
        Card secondCopy = new Ornithopter();
        Card otherCard = new WalkingBallista();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(firstCopy, otherCard, secondCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstCopy, secondCopy);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCopy, secondCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void maySearchForZeroCards() {
        Card target = new Ornithopter();
        Card libraryCopy = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCopy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetLand() {
        Card land = new SpireOfIndustry();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayStopAfterFindingOnlyOneOfSeveralCopies() {
        Card target = new Ornithopter();
        Card firstCopy = new Ornithopter();
        Card secondCopy = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(firstCopy, secondCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCopy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exilesTargetEvenWhenLibraryHasNoMatchingCards() {
        Card target = new Ornithopter();
        Card otherCard = new WalkingBallista();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(otherCard));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTargetNoncreatureCardAndFindAnotherCopy() {
        Card target = new SecretSalvage();
        Card libraryCopy = new SecretSalvage();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetCardInOpponentsGraveyard() {
        Card target = new Ornithopter();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSecretSalvageMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
