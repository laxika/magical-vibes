package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpyNetwork.class, ElvishWarrior.class})
class SpyNetworkTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the target player's hidden information and reorders the top four cards")
    void looksAtTargetInformationAndReordersOwnLibrary() {
        List<Card> targetHand = List.of(new ElvishWarrior());
        List<Card> targetLibrary = cards(1);
        List<Card> ownLibrary = cards(5);
        harness.setHand(player2, targetHand);
        harness.setLibrary(player2, targetLibrary);
        harness.setLibrary(player1, ownLibrary);
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent ownFaceDownCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        ownFaceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new SpyNetwork()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Elvish Warrior"));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anyMatch(message -> message.contains("Elvish Warrior"));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .singleElement()
                .satisfies(message -> assertThat(message).contains("Elvish Warrior"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyElementsOf(ownLibrary.subList(0, 4));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                ownLibrary.get(3), ownLibrary.get(1), ownLibrary.get(0), ownLibrary.get(2), ownLibrary.get(4));
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetSelf() {
        List<Card> ownLibrary = cards(4);
        harness.setLibrary(player1, ownLibrary);
        harness.setHand(player1, List.of(new SpyNetwork(), new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Elvish Warrior"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Does not look at face-down permanents that are not creatures")
    void ignoresFaceDownNoncreature() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ElvishWarrior()));
        harness.setLibrary(player1, cards(2));
        Permanent faceDownArtifact = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        faceDownArtifact.setFaceDown(0, 0, Set.of(CardType.ARTIFACT));
        harness.setHand(player1, List.of(new SpyNetwork()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    @Test
    @DisplayName("Looks at the available cards when either library has fewer than four cards")
    void handlesShortLibraries() {
        List<Card> ownLibrary = cards(2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, ownLibrary);
        harness.setHand(player1, List.of(new SpyNetwork()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyElementsOf(ownLibrary);
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new SpyNetwork()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only target players");
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new ElvishWarrior())
                .toList();
    }
}
