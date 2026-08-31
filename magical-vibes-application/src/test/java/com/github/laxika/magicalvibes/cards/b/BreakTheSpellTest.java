package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakTheSpell.class, GhostlyPrison.class, GrizzlyBears.class})
class BreakTheSpellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an enchantment you control and draws a card")
    void destroysOwnEnchantmentAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhostlyPrison());
        prepareSpell();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroys an opponent's enchantment without drawing")
    void destroysOpponentsEnchantmentWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        prepareSpell();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Destroys a token enchantment and draws a card")
    void destroysTokenEnchantmentAndDraws() {
        Card tokenCard = new Card();
        tokenCard.setName("Enchantment Token");
        tokenCard.setType(CardType.ENCHANTMENT);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, tokenCard);
        prepareSpell();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when the enchantment is indestructible")
    void indestructibleEnchantmentDoesNotDraw() {
        Card indestructibleCard = new Card();
        indestructibleCard.setName("Indestructible Enchantment");
        indestructibleCard.setType(CardType.ENCHANTMENT);
        indestructibleCard.setManaCost("");
        indestructibleCard.setKeywords(java.util.Set.of(Keyword.INDESTRUCTIBLE));
        Permanent target = harness.addToBattlefieldAndReturn(player1, indestructibleCard);
        prepareSpell();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakTheSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BreakTheSpell()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void cast(Permanent target) {
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
