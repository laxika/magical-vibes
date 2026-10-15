package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexpectedWindfall.class, Forest.class})
class UnexpectedWindfallTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card, draws two cards, and creates two Treasures")
    void discardsDrawsAndCreatesTwoTreasures() {
        harness.setHand(player1, List.of(new UnexpectedWindfall(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast without another card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new UnexpectedWindfall()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A nonland card is discarded before the spell resolves, even when it precedes the spell in hand")
    void paysNonlandDiscardBeforeResolution() {
        UnexpectedWindfall discarded = new UnexpectedWindfall();
        UnexpectedWindfall spell = new UnexpectedWindfall();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(discarded, spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithDiscard(player1, 1, null, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, spell);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Both Treasures can immediately be sacrificed for mana of different colors")
    void treasuresCanImmediatelyProduceMana() {
        harness.setHand(player1, List.of(new UnexpectedWindfall(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
