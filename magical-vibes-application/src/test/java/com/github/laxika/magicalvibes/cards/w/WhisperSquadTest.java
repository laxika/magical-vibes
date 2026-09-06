package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperSquad.class, GrizzlyBears.class})
class WhisperSquadTest extends BaseCardTest {

    @Test
    @DisplayName("The ability searches for a Whisper Squad and excludes other cards")
    void abilitySearchesForWhisperSquad() {
        addReadyWhisperSquad();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WhisperSquad()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Whisper Squad");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The searched Whisper Squad enters the battlefield tapped")
    void searchedWhisperSquadEntersTapped() {
        addReadyWhisperSquad();
        WhisperSquad searchedCard = new WhisperSquad();
        harness.setLibrary(player1, List.of(searchedCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Whisper Squad")).hasSize(2);
        Permanent searchedPermanent = findPermanents(player1, "Whisper Squad").stream()
                .filter(permanent -> permanent.getCard() == searchedCard)
                .findFirst()
                .orElseThrow();
        assertThat(searchedPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability does nothing when no Whisper Squad is in the library")
    void noWhisperSquadFound() {
        addReadyWhisperSquad();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Whisper Squad")).hasSize(1);
    }

    private void addReadyWhisperSquad() {
        Permanent squad = findPermanentAfterAdding();
        squad.setSummoningSick(false);
    }

    private Permanent findPermanentAfterAdding() {
        harness.addToBattlefield(player1, new WhisperSquad());
        return findPermanent(player1, "Whisper Squad");
    }
}
