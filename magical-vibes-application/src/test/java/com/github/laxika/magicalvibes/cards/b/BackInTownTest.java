package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DreadWarlock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackInTown.class, DreadWarlock.class, GrizzlyBears.class})
class BackInTownTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses exactly X outlaw creature cards from your graveyard")
    void choosesEligibleTargets() {
        Card warlock = new DreadWarlock();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(warlock, bears));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(warlock.getId());
    }

    @Test
    @DisplayName("Returns the selected outlaw creature cards to the battlefield")
    void returnsSelectedOutlaws() {
        Card warlock = new DreadWarlock();
        harness.setGraveyard(player1, List.of(warlock));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(warlock.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dread Warlock")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Back in Town");
    }

    @Test
    @DisplayName("X greater than the eligible outlaw count is illegal")
    void xGreaterThanEligibleCountThrows() {
        harness.setGraveyard(player1, List.of(new DreadWarlock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching creature cards in graveyard");
    }

    @Test
    @DisplayName("X=0 resolves without returning cards")
    void xZeroReturnsNothing() {
        Card warlock = new DreadWarlock();
        harness.setGraveyard(player1, List.of(warlock));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Dread Warlock", "Back in Town");
    }
}
