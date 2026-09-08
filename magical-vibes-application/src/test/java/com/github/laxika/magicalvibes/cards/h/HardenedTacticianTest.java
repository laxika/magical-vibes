package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HardenedTactician.class, Forest.class})
class HardenedTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a token and paying {1} draws a card")
    void sacrificesTokenAndDrawsCard() {
        harness.addToBattlefield(player1, new HardenedTactician());
        Permanent token = addToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(token.getCard());
    }

    @Test
    @DisplayName("Cannot activate without a token to sacrifice")
    void requiresTokenToSacrifice() {
        harness.addToBattlefield(player1, new HardenedTactician());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new HardenedTactician());
        addToken(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToken(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Soldier");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(player.getId()).add(token);
        return token;
    }
}
