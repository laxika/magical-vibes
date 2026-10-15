package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrillOfPossibility.class, Forest.class})
class ThrillOfPossibilityTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card as a cost, then draws two")
    void discardsThenDrawsTwo() {
        harness.setHand(player1, List.of(new ThrillOfPossibility(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast with no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new ThrillOfPossibility()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A cast missing the discard selection is rejected before any cost is paid")
    void rejectedCastLeavesManaAndHandUntouched() {
        harness.setHand(player1, List.of(new ThrillOfPossibility(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("The discard is paid before resolution and cards are drawn only on resolution")
    void discardIsPaidBeforeResolution() {
        Forest discarded = new Forest();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(new ThrillOfPossibility(), discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 0, null, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Thrill of Possibility");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonland card before the spell in hand can pay the discard cost")
    void canDiscardNonlandBeforeSpellInHand() {
        ThrillOfPossibility discarded = new ThrillOfPossibility();
        ThrillOfPossibility spell = new ThrillOfPossibility();
        harness.setHand(player1, List.of(discarded, spell));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithDiscard(player1, 1, null, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, spell);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
