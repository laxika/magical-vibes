package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizeTheSpoils.class, Forest.class})
class SeizeTheSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Discard and mana are paid before drawing cards or creating Treasure")
    void paysCostsBeforeResolution() {
        harness.setHand(player1, List.of(new SeizeTheSpoils(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can discard a nonland card preceding the spell in hand")
    void discardsNonlandBeforeSpellIndex() {
        SeizeTheSpoils discarded = new SeizeTheSpoils();
        SeizeTheSpoils cast = new SeizeTheSpoils();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(discarded, cast));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, cast);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Discards a card as a cost, then draws two and makes a Treasure")
    void discardsThenDrawsAndMakesTreasure() {
        harness.setHand(player1, List.of(new SeizeTheSpoils(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Discard the Forest (index 1 in the pre-cast hand) as the additional cost.
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        // Started with 2 cards, cast one, discarded one (net 0), then drew two.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Cannot be cast with no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SeizeTheSpoils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot use the spell itself to pay the discard cost")
    void cannotDiscardItselfEvenWithAnotherCardInHand() {
        SeizeTheSpoils spell = new SeizeTheSpoils();
        Forest otherCard = new Forest();
        harness.setHand(player1, List.of(spell, otherCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell, otherCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Seize the Spoils");
    }

    @Test
    @DisplayName("A cast missing the discard selection is rejected before any cost is paid")
    void rejectedCastLeavesManaAndHandUntouched() {
        harness.setHand(player1, List.of(new SeizeTheSpoils(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");

        // An illegal casting attempt restores the hand and leaves mana unspent.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
