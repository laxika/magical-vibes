package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeerPastTheVeil.class, GrizzlyBears.class, Island.class, Ornithopter.class, Shock.class})
class PeerPastTheVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the hand, then draws for each distinct card type in the graveyard")
    void discardsHandThenDrawsForDistinctGraveyardTypes() {
        Card spell = new PeerPastTheVeil();
        Card creature = new GrizzlyBears();
        Card land = new Island();
        Card instant = new Shock();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Island();
        Card thirdDraw = new Shock();
        harness.setHand(player1, List.of(spell, creature, land, instant));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, creature, land, instant);
    }

    @Test
    @DisplayName("Counts each card type on a multi-type card")
    void countsMultiTypeCards() {
        Card spell = new PeerPastTheVeil();
        Card artifactCreature = new Ornithopter();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(spell, artifactCreature));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, artifactCreature);
    }

    @Test
    @DisplayName("Draws no cards when the graveyard has no card types after discarding")
    void drawsNothingWithEmptyGraveyardAndHand() {
        Card spell = new PeerPastTheVeil();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Draws for pre-existing graveyard types even with an empty hand")
    void drawsForExistingGraveyardWithEmptyHand() {
        Card spell = new PeerPastTheVeil();
        Card artifactCreature = new Ornithopter();
        Card land = new Island();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Island();
        Card thirdDraw = new Shock();
        Card remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifactCreature, land));
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw, remaining));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(artifactCreature, land, spell);
    }

    @Test
    @DisplayName("Counts overlapping types once and ignores the opponent's graveyard")
    void countsDistinctOwnTypesOnly() {
        Card spell = new PeerPastTheVeil();
        Card creature = new GrizzlyBears();
        Card duplicateCreature = new GrizzlyBears();
        Card artifactCreature = new Ornithopter();
        Card opponentInstant = new Shock();
        Card opponentLand = new Island();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Island();
        Card remaining = new Shock();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentInstant, opponentLand));
        harness.setHand(player1, List.of(spell, duplicateCreature, artifactCreature));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remaining));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, duplicateCreature, artifactCreature, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentInstant, opponentLand);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
