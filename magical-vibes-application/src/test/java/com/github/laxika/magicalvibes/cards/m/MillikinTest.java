package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Millikin.class)
class MillikinTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps, mills 1 card, and adds {C}")
    void activateAbilityMillsAndAddsColorless() {
        Permanent millikin = addCreatureReady(player1, new Millikin());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(millikin.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The milled card goes to the graveyard")
    void milledCardGoesToGraveyard() {
        Permanent millikin = addCreatureReady(player1, new Millikin());

        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).extracting(Card::getName).contains(topCard.getName());
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Millikin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        Permanent millikin = addCreatureReady(player1, new Millikin());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with empty library")
    void cannotActivateWithEmptyLibrary() {
        addCreatureReady(player1, new Millikin());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to mill");
    }
}
