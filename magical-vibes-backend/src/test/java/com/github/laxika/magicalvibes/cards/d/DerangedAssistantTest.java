package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerangedAssistantTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps, mills 1 card, and adds {C}")
    void activateAbilityMillsAndAddsColorless() {
        harness.addToBattlefield(player1, new DerangedAssistant());
        GameData gd = harness.getGameData();
        Permanent assistant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assistant.setSummoningSick(false);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(assistant.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty(); // mana ability resolves immediately
    }

    @Test
    @DisplayName("The milled card goes to the graveyard")
    void milledCardGoesToGraveyard() {
        harness.addToBattlefield(player1, new DerangedAssistant());
        GameData gd = harness.getGameData();
        Permanent assistant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assistant.setSummoningSick(false);

        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).extracting(Card::getName).contains(topCard.getName());
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DerangedAssistant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new DerangedAssistant());
        GameData gd = harness.getGameData();
        Permanent assistant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assistant.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with empty library")
    void cannotActivateWithEmptyLibrary() {
        harness.addToBattlefield(player1, new DerangedAssistant());
        GameData gd = harness.getGameData();
        Permanent assistant = gd.playerBattlefields.get(player1.getId()).getFirst();
        assistant.setSummoningSick(false);
        gd.playerDecks.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to mill");
    }
}
