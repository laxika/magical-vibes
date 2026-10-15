package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForcedRetreat.class, Forest.class, ForestBear.class})
class ForcedRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new ForcedRetreat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Forced Retreat puts target creature on top of its owner's library")
    void resolvingPutsTargetCreatureOnTopOfOwnersLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ForestBear()).getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ForcedRetreat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertNotInGraveyard(player2, "Forest Bear");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Forest Bear");
        harness.assertInGraveyard(player1, "Forced Retreat");
    }

    @Test
    @DisplayName("Can put your own creature on top of an empty library")
    void canTargetOwnCreatureWithEmptyLibrary() {
        ForestBear bear = new ForestBear();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, bear).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ForcedRetreat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest Bear");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        harness.assertNotInGraveyard(player1, "Forest Bear");
        harness.assertInGraveyard(player1, "Forced Retreat");
    }

    @Test
    @DisplayName("A creature controlled by another player goes to its owner's library without shuffling")
    void controlledCreatureReturnsToOwnersLibrary() {
        ForestBear bear = new ForestBear();
        bear.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, bear).getId();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new ForcedRetreat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear, first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        harness.assertNotInGraveyard(player1, "Forest Bear");
        harness.assertNotInGraveyard(player2, "Forest Bear");
        harness.assertInGraveyard(player1, "Forced Retreat");
    }

    @Test
    @DisplayName("Forced Retreat fizzles if the target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ForestBear()).getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ForcedRetreat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Forced Retreat");
    }
}
