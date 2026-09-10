package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Borrowing100000Arrows.class, Island.class, ShuFootSoldiers.class})
class Borrowing100000ArrowsTest extends BaseCardTest {

    private void castArrows() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws a card for each tapped creature the target opponent controls")
    void drawsPerTappedCreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        Permanent tapped1 = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        Permanent tapped2 = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.addToBattlefield(player2, new ShuFootSoldiers()); // untapped -> not counted
        tapped1.tap();
        tapped2.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castArrows();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Draws nothing when the opponent controls no tapped creatures")
    void drawsNothingWithoutTappedCreatures() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        harness.addToBattlefield(player2, new ShuFootSoldiers()); // untapped
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castArrows();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Does not count tapped noncreatures")
    void ignoresTappedNoncreatures() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Island());
        tappedLand.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castArrows();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Counts creatures based on their state as the spell resolves")
    void countsAtResolution() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        creature.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Only the target opponent's tapped creatures are counted")
    void ignoresControllersTappedCreatures() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        Permanent ownTapped = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        ownTapped.tap();
        Permanent oppTapped = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        oppTapped.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castArrows();

        // Only the opponent's one tapped creature counts.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new Borrowing100000Arrows())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
