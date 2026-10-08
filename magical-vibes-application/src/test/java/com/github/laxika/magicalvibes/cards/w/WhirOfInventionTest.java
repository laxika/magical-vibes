package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AetherSwooper;
import com.github.laxika.magicalvibes.cards.i.ImplementOfExamination;
import com.github.laxika.magicalvibes.cards.i.ImplementOfFerocity;
import com.github.laxika.magicalvibes.cards.i.ImplementOfMalice;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirOfInvention.class, ImplementOfFerocity.class, Ornithopter.class, ImplementOfMalice.class,
        ImplementOfExamination.class, AetherSwooper.class})
class WhirOfInventionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only artifacts with mana value <= X")
    void presentsOnlyArtifactsWithinManaValueBound() {
        castWhir(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Ornithopter", "Implement of Ferocity", "Implement of Malice");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Choosing an artifact puts it onto the battlefield")
    void chosenArtifactEntersBattlefield() {
        castWhir(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosen));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosen));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Whir of Invention");
    }

    @Test
    @DisplayName("Improvise lets an artifact pay the generic part of Whir of Invention")
    void improvisePaysGenericMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfFerocity());
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 1, null, null,
                List.of(), List.of(artifact.getId()), false, null);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=0 finds zero-mana-value artifacts only")
    void xZeroFindsZeroManaValueArtifactsOnly() {
        castWhir(0);
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData()
                .interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Ornithopter");
    }

    @Test
    void mayFailToFindEvenWhenAnArtifactQualifies() {
        harness.setLibrary(player1, List.of(new Ornithopter()));
        castWhir(0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Ornithopter");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Whir of Invention");
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        castWhir(0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Whir of Invention");
    }

    @Test
    void newlyEnteredArtifactCreatureCanImproviseWithoutReducingSearchX() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new ImplementOfFerocity(), new ImplementOfMalice()));
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 1, null, null,
                List.of(), List.of(artifact.getId()), false, null);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Implement of Ferocity");
        harness.handleCardChosen(player1, 0);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Implement of Ferocity") && !p.isTapped());
    }

    @Test
    void improviseCannotPayBlueMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1,
                0, 1, null, null, List.of(), List.of(artifact.getId()), false, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void alreadyTappedArtifactCannotImprovise() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1,
                0, 1, null, null, List.of(), List.of(artifact.getId()), false, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void castWhir(int xValue) {
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 3);
        harness.castInstant(player1, 0, xValue, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Ornithopter(), new ImplementOfFerocity(), new ImplementOfMalice(),
                new ImplementOfExamination(), new AetherSwooper()));
    }
}
