package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.s.SensoryDeprivation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeDreams.class, Pacifism.class, Rancor.class, SensoryDeprivation.class, GrizzlyBears.class})
class ThreeDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to three Aura cards with different names and reveals them")
    void offersDifferentNamedAuras() {
        setupLibrary(new Pacifism(), new Pacifism(), new Rancor(), new SensoryDeprivation(), new GrizzlyBears());
        cast();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);
        assertThat(search.params().cards()).allMatch(Card::isAura);
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().requireDifferentNames()).isTrue();
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Puts three differently named Auras into hand")
    void choosesThreeDifferentAuras() {
        setupLibrary(new Pacifism(), new Pacifism(), new Rancor(), new SensoryDeprivation());
        cast();

        harness.passBothPriorities();
        chooseCard("Pacifism");

        assertThat(offeredNames()).doesNotContain("Pacifism");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount())
                .isEqualTo(2);

        chooseCard("Rancor");
        chooseCard("Sensory Deprivation");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).contains("Pacifism", "Rancor", "Sensory Deprivation");
    }

    @Test
    @DisplayName("Does not create an interaction when the library has no Aura")
    void noAuraNoInteraction() {
        setupLibrary(new GrizzlyBears());
        cast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new ThreeDreams()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(Arrays.asList(cards));
    }

    private void chooseCard(String name) {
        List<Card> cards = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        int index = cards.stream().map(Card::getName).toList().indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> offeredNames() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .map(Card::getName)
                .toList();
    }

    private List<String> handNames() {
        return gd.playerHands.get(player1.getId()).stream().map(Card::getName).toList();
    }
}
