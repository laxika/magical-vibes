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
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }

    @Test
    @DisplayName("A noncreature token can pay the sacrifice cost before the draw resolves")
    void sacrificesNoncreatureTokenAsCost() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new HardenedTactician());
        tactician.tap();
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, treasure);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("An opponent's token cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsToken() {
        harness.addToBattlefield(player1, new HardenedTactician());
        Permanent opposingToken = addToken(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingToken);
    }

    @Test
    @DisplayName("A nontoken permanent cannot pay the sacrifice cost")
    void cannotSacrificeNontokenPermanent() {
        harness.addToBattlefield(player1, new HardenedTactician());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability can be used repeatedly without tapping the tactician")
    void canActivateRepeatedly() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new HardenedTactician());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        addToken(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addToken(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(tactician);
        assertThat(tactician.isTapped()).isFalse();
    }
}
