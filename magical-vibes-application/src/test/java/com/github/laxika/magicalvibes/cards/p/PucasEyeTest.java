package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PucasEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws a card, then choosing a color makes Puca's Eye that color")
    void entersDrawsThenBecomesChosenColor() {
        harness.setHand(player1, List.of(new PucasEye()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();
        int deckBeforeCast = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeCast - 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent eye = findPermanent(player1, "Puca's Eye");
        assertThat(eye.getChosenColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, eye)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The draw ability works when permanents provide all five colors")
    void drawAbilityRequiresAndUsesFiveColors() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PucasEye());
        eye.setChosenColor(CardColor.GREEN);
        addColoredPermanent(player1, CardColor.WHITE);
        addColoredPermanent(player1, CardColor.BLUE);
        addColoredPermanent(player1, CardColor.BLACK);
        addColoredPermanent(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(eye.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The draw ability cannot be activated without all five colors")
    void drawAbilityRequiresAllFiveColors() {
        harness.addToBattlefieldAndReturn(player1, new PucasEye());
        addColoredPermanent(player1, CardColor.WHITE);
        addColoredPermanent(player1, CardColor.BLUE);
        addColoredPermanent(player1, CardColor.BLACK);
        addColoredPermanent(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("five colors");
    }

    private void addColoredPermanent(Player player, CardColor color) {
        Card card = new Card();
        card.setName(color.name() + " permanent");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setColors(List.of(color));
        card.setPower(1);
        card.setToughness(1);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
