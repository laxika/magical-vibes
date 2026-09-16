package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grief.class, Forest.class, ChildOfNight.class})
class GriefTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals an opponent's hand and lets you discard a chosen nonland card")
    void etbDiscardsChosenNonlandCard() {
        Card land = new Forest();
        Card nonland = new Grief();
        harness.setHand(player2, new ArrayList<>(List.of(land, nonland)));
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("A land-only hand gives no discard choice")
    void landOnlyHandCannotBeChosen() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grief");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Evoke exiles a black card and sacrifices Grief as it enters")
    void evokeExilesBlackCardAndSacrificesSelf() {
        Card blackCard = new ChildOfNight();
        harness.setHand(player1, List.of(new Grief(), blackCard));
        Card discard = new Grief();
        harness.setHand(player2, List.of(discard));

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null,
                List.of(), List.of(), false, null, null, List.of(), null, null, false, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(blackCard);
        harness.assertInGraveyard(player1, "Grief");
        harness.assertNotOnBattlefield(player1, "Grief");
    }
}
