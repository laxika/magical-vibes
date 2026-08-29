package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LetterOfAcceptanceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one mana of a chosen color")
    void tappingAddsChosenColor() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        Permanent letter = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThat(letter.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Pays {2}, sacrifices itself, and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LetterOfAcceptance);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("The draw ability requires two mana")
    void drawAbilityRequiresTwoMana() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
