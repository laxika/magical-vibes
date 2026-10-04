package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FontOfFortunes.class, GrizzlyBears.class, Island.class})
class FontOfFortunesTest extends BaseCardTest {

    @Test
    @DisplayName("sacrificing Font of Fortunes draws two cards")
    void sacrificingFontOfFortunesDrawsTwoCards() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFortunes());
        GrizzlyBears firstCard = new GrizzlyBears();
        Island secondCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards are drawn only on resolution even when the Font is tapped")
    void tappedFontDrawsOnlyOnResolution() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFortunes());
        font.tap();
        FontOfFortunes firstCard = new FontOfFortunes();
        FontOfFortunes secondCard = new FontOfFortunes();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(font.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the blue activation cost")
    void activationRequiresBlueMana() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFortunes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(font.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One blue mana alone cannot pay the whole activation cost")
    void activationRequiresGenericManaToo() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFortunes());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(font);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(font.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
