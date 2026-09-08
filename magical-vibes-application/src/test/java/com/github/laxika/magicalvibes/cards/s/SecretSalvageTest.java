package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretSalvageTest extends BaseCardTest {

    @Test
    void exilesTargetAndSearchesForAnyNumberOfSameNameCards() {
        Card target = new GrizzlyBears();
        Card firstCopy = new GrizzlyBears();
        Card secondCopy = new GrizzlyBears();
        Card otherCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(firstCopy, otherCard, secondCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstCopy, secondCopy);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCopy, secondCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void maySearchForZeroCards() {
        Card target = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCopy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetLand() {
        Card land = new Plains();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SecretSalvage()));
        addSecretSalvageMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addSecretSalvageMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
